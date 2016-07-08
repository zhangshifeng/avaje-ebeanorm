package com.avaje.ebean;

import java.io.Serializable;
import java.sql.ResultSet;
import java.util.*;

import com.avaje.ebean.util.CamelCaseHelper;

/**
 * Used to build object graphs based on a raw SQL statement (rather than
 * generated by Ebean).
 * <p>
 * If you don't want to build object graphs you can use {@link SqlQuery} instead
 * which returns {@link SqlRow} objects rather than entity beans.
 * </p>
 * <p>
 * <b>Unparsed RawSql:</b>
 * </p>
 * <p>
 * When RawSql is created via {@link RawSqlBuilder#unparsed(String)} then Ebean can not
 * modify the SQL at all. It can't add any extra expressions into the SQL.
 * </p>
 * <p>
 * <b>Parsed RawSql:</b>
 * </p>
 * <p>
 * When RawSql is created via {@link RawSqlBuilder#parse(String)} then Ebean will parse the
 * SQL and find places in the SQL where it can add extra where expressions, add
 * extra having expressions or replace the order by clause. If you want to
 * explicitly tell Ebean where these insertion points are you can place special
 * strings into your SQL ({@code ${where}} or {@code ${andWhere}} and {@code ${having}} or
 * {@code ${andHaving})}.
 * </p>
 * <p>
 * If the SQL already includes a WHERE clause put in {@code ${andWhere}} in the location
 * you want Ebean to add any extra where expressions. If the SQL doesn't have a
 * WHERE clause put {@code ${where}} in instead. Similarly you can put in {@code ${having}} or
 * {@code ${andHaving}} where you want Ebean put add extra having expressions.
 * </p>
 * <p>
 * <b>Aggregates:</b>
 * </p>
 * <p>
 * Often RawSql will be used with Aggregate functions (sum, avg, max etc). The
 * follow example shows an example based on Total Order Amount -
 * sum(d.order_qty*d.unit_price).
 * </p>
 * <p>
 * We can use a OrderAggregate bean that has a &#064;Sql to indicate it is based
 * on RawSql and not based on a real DB Table or DB View. It has some properties
 * to hold the values for the aggregate functions (sum etc) and a &#064;OneToOne
 * to Order.
 * </p>
 *
 * <h3>Example OrderAggregate</h3>
 * 
 * <pre>{@code
 *  ...
 *  // @Sql indicates to that this bean
 *  // is based on RawSql rather than a table
 * 
 * @Entity
 * @Sql
 * public class OrderAggregate {
 * 
 *  @OneToOne
 *  Order order;
 *      
 *  Double totalAmount;
 *  
 *  Double totalItems;
 *  
 *  // getters and setters
 *  ...
 *
 * }</pre>
 *
 * <h3>Example 1:</h3>
 * 
 * <pre>{@code
 *
 *   String sql = " select order_id, o.status, c.id, c.name, sum(d.order_qty*d.unit_price) as totalAmount"
 *     + " from o_order o"
 *     + " join o_customer c on c.id = o.kcustomer_id "
 *     + " join o_order_detail d on d.order_id = o.id " + " group by order_id, o.status ";
 * 
 *   RawSql rawSql = RawSqlBuilder.parse(sql)
 *     // map the sql result columns to bean properties
 *     .columnMapping("order_id", "order.id")
 *     .columnMapping("o.status", "order.status")
 *     .columnMapping("c.id", "order.customer.id")
 *     .columnMapping("c.name", "order.customer.name")
 *     // we don't need to map this one due to the sql column alias
 *     // .columnMapping("sum(d.order_qty*d.unit_price)", "totalAmount")
 *     .create();
 * 
 *   List<OrderAggregate> list = Ebean.find(OrderAggregate.class)
 *       .setRawSql(rawSql)
 *       .where().gt("order.id", 0)
 *       .having().gt("totalAmount", 20)
 *       .findList();
 * 
 *
 * }</pre>
 * 
 * <h3>Example 2:</h3>
 * 
 * <p>
 * The following example uses a FetchConfig().query() so that after the initial
 * RawSql query is executed Ebean executes a secondary query to fetch the
 * associated order status, orderDate along with the customer name.
 * </p>
 * 
 * <pre>{@code
 *
 *  String sql = " select order_id, 'ignoreMe', sum(d.order_qty*d.unit_price) as totalAmount "
 *     + " from o_order_detail d"
 *     + " group by order_id ";
 * 
 *   RawSql rawSql = RawSqlBuilder.parse(sql)
 *     .columnMapping("order_id", "order.id")
 *     .columnMappingIgnore("'ignoreMe'")
 *     .create();
 * 
 *   List<OrderAggregate> orders = Ebean.find(OrderAggregate.class)
 *     .setRawSql(rawSql)
 *     .fetch("order", "status,orderDate", new FetchConfig().query())
 *     .fetch("order.customer", "name")
 *     .where().gt("order.id", 0)
 *     .having().gt("totalAmount", 20)
 *     .order().desc("totalAmount")
 *     .setMaxRows(10)
 *     .findList();
 * 
 * }</pre>
 *
 *
 * <h3>Example 3: tableAliasMapping</h3>
 * <p>
 *   Instead of mapping each column you can map each table alias to a path using tableAliasMapping().
 * </p>
 * <pre>{@code
 *
 *   String rs = "select o.id, o.status, c.id, c.name, "+
 *               " d.id, d.order_qty, p.id, p.name " +
 *               "from o_order o join o_customer c on c.id = o.kcustomer_id " +
 *               "join o_order_detail d on d.order_id = o.id  " +
 *               "join o_product p on p.id = d.product_id  " +
 *               "where o.id <= :maxOrderId  and p.id = :productId "+
 *               "order by o.id, d.id asc";
 *
 *  RawSql rawSql = RawSqlBuilder.parse(rs)
 *       .tableAliasMapping("c", "customer")
 *       .tableAliasMapping("d", "details")
 *       .tableAliasMapping("p", "details.product")
 *       .create();
 *
 *  List<Order> ordersFromRaw = Ebean.find(Order.class)
 *       .setRawSql(rawSql)
 *       .setParameter("maxOrderId", 2)
 *       .setParameter("productId", 1)
 *       .findList();
 *
 * }</pre>
 *
 *
 * <p>
 * Note that lazy loading also works with object graphs built with RawSql.
 * </p>
 * 
 */
public final class RawSql implements Serializable {

  private static final long serialVersionUID = 1L;

  private final ResultSet resultSet;
  
  private final Sql sql;

  private final ColumnMapping columnMapping;

  /**
   * Construct with a ResultSet and properties that the columns map to.
   * <p>
   * The properties listed in the propertyNames must be in the same order as the columns in the
   * resultSet.
   * <p>
   * When a query executes this RawSql object then it will close the resultSet.
   */
  public RawSql(ResultSet resultSet, String... propertyNames) {
    this.resultSet = resultSet;
    this.sql = null;
    this.columnMapping = new ColumnMapping(propertyNames);
  }
  
  protected RawSql(ResultSet resultSet, Sql sql, ColumnMapping columnMapping) {
    this.resultSet = resultSet;
    this.sql = sql;
    this.columnMapping = columnMapping;
  }

  /**
   * Return the Sql either unparsed or in parsed (broken up) form.
   */
  public Sql getSql() {
    return sql;
  }

  /**
   * Return the key;
   */
  public Key getKey() {
    boolean parsed = sql != null && sql.parsed;
    String unParsedSql = (sql == null) ? "" : sql.unparsedSql;
    return new Key(parsed, unParsedSql, columnMapping);
  }
  
  /**
   * Return the resultSet if this is a ResultSet based RawSql.
   */
  public ResultSet getResultSet() {
    return resultSet;
  }

  /**
   * Return the column mapping for the SQL columns to bean properties.
   */
  public ColumnMapping getColumnMapping() {
    return columnMapping;
  }

  /**
   * Represents the sql part of the query. For parsed RawSql the sql is broken
   * up so that Ebean can insert extra WHERE and HAVING expressions into the
   * SQL.
   */
  public static final class Sql implements Serializable {

    private static final long serialVersionUID = 1L;

    private final boolean parsed;

    private final String unparsedSql;

    private final String preFrom;

    private final String preWhere;

    private final boolean andWhereExpr;

    private final String preHaving;

    private final boolean andHavingExpr;

    private final String orderByPrefix;

    private final String orderBy;

    private final boolean distinct;

    /**
     * Construct for unparsed SQL.
     */
    protected Sql(String unparsedSql) {
      this.parsed = false;
      this.unparsedSql = unparsedSql;
      this.preFrom = null;
      this.preHaving = null;
      this.preWhere = null;
      this.andHavingExpr = false;
      this.andWhereExpr = false;
      this.orderByPrefix = null;
      this.orderBy = null;
      this.distinct = false;
    }

    /**
     * Construct for parsed SQL.
     */
    protected Sql(String unparsedSql, String preFrom, String preWhere, boolean andWhereExpr,
        String preHaving, boolean andHavingExpr, String orderByPrefix, String orderBy, boolean distinct) {

      this.unparsedSql = unparsedSql;
      this.parsed = true;
      this.preFrom = preFrom;
      this.preHaving = preHaving;
      this.preWhere = preWhere;
      this.andHavingExpr = andHavingExpr;
      this.andWhereExpr = andWhereExpr;
      this.orderByPrefix = orderByPrefix;
      this.orderBy = orderBy;
      this.distinct = distinct;
    }

    public String toString() {
      if (!parsed) {
        return "unparsed[" + unparsedSql + "]";
      }
      return "select[" + preFrom + "] preWhere[" + preWhere + "] preHaving[" + preHaving
          + "] orderBy[" + orderBy + "]";
    }

    public boolean isDistinct() {
      return distinct;
    }

    /**
     * Return true if the SQL is left completely unmodified.
     * <p>
     * This means Ebean can't add WHERE or HAVING expressions into the query -
     * it will be left completely unmodified.
     * </p>
     */
    public boolean isParsed() {
      return parsed;
    }

    /**
     * Return the SQL when it is unparsed.
     */
    public String getUnparsedSql() {
      return unparsedSql;
    }

    /**
     * Return the SQL prior to FROM clause.
     */
    public String getPreFrom() {
      return preFrom;
    }

    /**
     * Return the SQL prior to WHERE clause.
     */
    public String getPreWhere() {
      return preWhere;
    }

    /**
     * Return true if there is already a WHERE clause and any extra where
     * expressions start with AND.
     */
    public boolean isAndWhereExpr() {
      return andWhereExpr;
    }

    /**
     * Return the SQL prior to HAVING clause.
     */
    public String getPreHaving() {
      return preHaving;
    }

    /**
     * Return true if there is already a HAVING clause and any extra having
     * expressions start with AND.
     */
    public boolean isAndHavingExpr() {
      return andHavingExpr;
    }

    /**
     * Return the 'order by' keywords.
     * This can contain additional keywords, for example 'order siblings by' as Oracle syntax.
     */
    public String getOrderByPrefix() {
      return (orderByPrefix == null) ? "order by" : orderByPrefix;
    }

    /**
     * Return the SQL ORDER BY clause.
     */
    public String getOrderBy() {
      return orderBy;
    }

  }

  /**
   * Defines the column mapping for raw sql DB columns to bean properties.
   */
  public static final class ColumnMapping implements Serializable {

    private static final long serialVersionUID = 1L;

    private final LinkedHashMap<String, Column> dbColumnMap;

    private final Map<String, String> propertyMap;
    
    private final Map<String, Column> propertyColumnMap;

    private final boolean parsed;

    private final boolean immutable;

    /**
     * Construct from parsed sql where the columns have been identified.
     */
    protected ColumnMapping(List<Column> columns) {
      this.immutable = false;
      this.parsed = true;
      this.propertyMap = null;
      this.propertyColumnMap = null;
      this.dbColumnMap = new LinkedHashMap<String, Column>();
      for (int i = 0; i < columns.size(); i++) {
        Column c = columns.get(i);
        dbColumnMap.put(c.getDbColumnKey(), c);
      }
    }

    /**
     * Construct for unparsed sql.
     */
    protected ColumnMapping() {
      this.immutable = false;
      this.parsed = false;
      this.propertyMap = null;
      this.propertyColumnMap = null;
      this.dbColumnMap = new LinkedHashMap<String, Column>();
    }
    
    /**
     * Construct for ResultSet use.
     */
    protected ColumnMapping(String... propertyNames) {
      this.immutable = false;
      this.parsed = false;
      this.propertyMap = null;
      this.dbColumnMap = new LinkedHashMap<String, Column>();

      int pos = 0;
      for (String prop : propertyNames) {
        dbColumnMap.put(prop, new Column(pos++, prop, null, prop));
      }
      propertyColumnMap = dbColumnMap;
    }

    /**
     * Construct an immutable ColumnMapping based on collected information.
     */
    protected ColumnMapping(boolean parsed, LinkedHashMap<String, Column> dbColumnMap) {
      this.immutable = true;
      this.parsed = parsed;
      this.dbColumnMap = dbColumnMap;

      HashMap<String, Column> pcMap = new HashMap<String, Column>();
      HashMap<String, String> pMap = new HashMap<String, String>();

      for (Column c : dbColumnMap.values()) {
        pMap.put(c.getPropertyName(), c.getDbColumn());
        pcMap.put(c.getPropertyName(), c);
      }
      this.propertyMap = Collections.unmodifiableMap(pMap);
      this.propertyColumnMap = Collections.unmodifiableMap(pcMap);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      ColumnMapping that = (ColumnMapping) o;
      return dbColumnMap.equals(that.dbColumnMap);
    }

    @Override
    public int hashCode() {
      return dbColumnMap.hashCode();
    }

    /**
     * Return true if the property is mapped.
     */
    public boolean contains(String property) {
      return this.propertyColumnMap.containsKey(property);
    }

    /**
     * Creates an immutable copy of this ColumnMapping.
     * 
     * @throws IllegalStateException
     *           when a propertyName has not been defined for a column.
     */
    protected ColumnMapping createImmutableCopy() {

      for (Column c : dbColumnMap.values()) {
        c.checkMapping();
      }

      return new ColumnMapping(parsed, dbColumnMap);
    }

    protected void columnMapping(String dbColumn, String propertyName) {

      if (immutable) {
        throw new IllegalStateException("Should never happen");
      }
      if (!parsed) {
        int pos = dbColumnMap.size();
        dbColumnMap.put(dbColumn, new Column(pos, dbColumn, null, propertyName));
      } else {
        Column column = dbColumnMap.get(dbColumn);
        if (column == null) {
          String msg = "DB Column [" + dbColumn + "] not found in mapping. Expecting one of [" + dbColumnMap.keySet() + "]";
          throw new IllegalArgumentException(msg);
        }
        column.setPropertyName(propertyName);
      }
    }

    /**
     * Returns true if the Columns where supplied by parsing the sql select
     * clause.
     * <p>
     * In the case where the columns where parsed then we can do extra checks on
     * the column mapping such as, is the column a valid one in the sql and
     * whether all the columns in the sql have been mapped.
     * </p>
     */
    public boolean isParsed() {
      return parsed;
    }

    /**
     * Return the number of columns in this column mapping.
     */
    public int size() {
      return dbColumnMap.size();
    }

    /**
     * Return the column mapping.
     */
    protected Map<String, Column> mapping() {
      return dbColumnMap;
    }

    /**
     * Return the mapping by DB column.
     */
    public Map<String, String> getMapping() {
      return propertyMap;
    }

    /**
     * Return the index position by bean property name.
     */
    public int getIndexPosition(String property) {
      Column c = propertyColumnMap.get(property);
      return c == null ? -1 : c.getIndexPos();
    }

    /**
     * Return an iterator of the Columns.
     */
    public Iterator<Column> getColumns() {
      return dbColumnMap.values().iterator();
    }

    /**
     * Modify any column mappings with the given table alias to have the path prefix.
     * <p>
     * For example modify all mappings with table alias "c" to have the path prefix "customer".
     * </p>
     * <p>
     * For the "Root type" you don't need to specify a tableAliasMapping.
     * </p>
     */
    public void tableAliasMapping(String tableAlias, String path) {

      String startMatch = tableAlias+".";
      for (Map.Entry<String, Column> entry : dbColumnMap.entrySet()) {
        if (entry.getKey().startsWith(startMatch)) {
          entry.getValue().tableAliasMapping(path);
        }
      }
    }

    /**
     * A Column of the RawSql that is mapped to a bean property (or ignored).
     */
    public static class Column implements Serializable {

      private static final long serialVersionUID = 1L;
      private final int indexPos;
      private final String dbColumn;

      private final String dbAlias;

      private String propertyName;

      /**
       * Construct a Column.
       */
      public Column(int indexPos, String dbColumn, String dbAlias) {
        this(indexPos, dbColumn, dbAlias, derivePropertyName(dbAlias, dbColumn));
      }

      private Column(int indexPos, String dbColumn, String dbAlias, String propertyName) {
        this.indexPos = indexPos;
        this.dbColumn = dbColumn;
        this.dbAlias = dbAlias;
        if (propertyName == null && dbAlias != null) {
          this.propertyName = dbAlias;
        } else {
          this.propertyName = propertyName;
        }
      }

      protected static String derivePropertyName(String dbAlias, String dbColumn) {
        if (dbAlias != null) {
          return CamelCaseHelper.toCamelFromUnderscore(dbAlias);
        }
        int dotPos = dbColumn.indexOf('.');
        if (dotPos > -1) {
          dbColumn = dbColumn.substring(dotPos + 1);
        }
        return CamelCaseHelper.toCamelFromUnderscore(dbColumn);
      }

      private void checkMapping() {
        if (propertyName == null) {
          String msg = "No propertyName defined (Column mapping) for dbColumn [" + dbColumn + "]";
          throw new IllegalStateException(msg);
        }
      }

      @Override
      public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Column that = (Column) o;
        if (indexPos != that.indexPos) return false;
        if (!dbColumn.equals(that.dbColumn)) return false;
        if (dbAlias != null ? !dbAlias.equals(that.dbAlias) : that.dbAlias != null) return false;
        return propertyName != null ? propertyName.equals(that.propertyName) : that.propertyName == null;
      }

      @Override
      public int hashCode() {
        int result = indexPos;
        result = 31 * result + dbColumn.hashCode();
        result = 31 * result + (dbAlias != null ? dbAlias.hashCode() : 0);
        result = 31 * result + (propertyName != null ? propertyName.hashCode() : 0);
        return result;
      }

      public String toString() {
        return dbColumn + "->" + propertyName;
      }

      /**
       * Return the index position of this column.
       */
      public int getIndexPos() {
        return indexPos;
      }

      /**
       * Return the DB column alias if specified otherwise DB column.
       * This is used as the key for mapping a column to a logical property.
       */
      public String getDbColumnKey() {
        return (dbAlias != null) ? dbAlias : dbColumn;
      }

      /**
       * Return the DB column name including table alias (if it has one).
       */
      public String getDbColumn() {
        return dbColumn;
      }

      /**
       * Return the bean property this column is mapped to.
       */
      public String getPropertyName() {
        return propertyName;
      }

      /**
       * Set the property name mapped to this db column.
       */
      private void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
      }

      /**
       * Prepend the path to the property name.
       * <p/>
       * For example if path is "customer" then "name" becomes "customer.name".
       */
      public void tableAliasMapping(String path) {
        if (path != null) {
          propertyName = path + "." + propertyName;
        }
      }
    }
  }

  /**
   * A key for the RawSql object using for the query plan.
   */
  public static final class Key {

    private final boolean parsed;
    private final ColumnMapping columnMapping;
    private final String unParsedSql;

    Key(boolean parsed, String unParsedSql, ColumnMapping columnMapping) {
      this.parsed = parsed;
      this.unParsedSql = unParsedSql;
      this.columnMapping = columnMapping;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Key that = (Key) o;
      return parsed == that.parsed
          && columnMapping.equals(that.columnMapping)
          && unParsedSql.equals(that.unParsedSql);
    }

    @Override
    public int hashCode() {
      int result = (parsed ? 1 : 0);
      result = 31 * result + columnMapping.hashCode();
      result = 31 * result + unParsedSql.hashCode();
      return result;
    }
  }
}
