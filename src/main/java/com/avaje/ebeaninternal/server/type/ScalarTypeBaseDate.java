package com.avaje.ebeaninternal.server.type;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Types;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

/**
 * Base class for Date types.
 */
public abstract class ScalarTypeBaseDate<T> extends ScalarTypeBase<T> {

  public ScalarTypeBaseDate(Class<T> type, boolean jdbcNative, int jdbcType) {
    super(type, jdbcNative, jdbcType);
  }

  /**
   * Convert the target value to millis.
   */
  public abstract long convertToMillis(Object value);

  /**
   * Convert to java.sql.Date from the target Date type.
   */
  public abstract java.sql.Date convertToDate(T t);

  /**
   * Convert from java.sql.Date to the target Date type.
   */
  public abstract T convertFromDate(java.sql.Date ts);

  public void bind(DataBind b, T value) throws SQLException {
    if (value == null) {
      b.setNull(Types.DATE);
    } else {
      Date date = convertToDate(value);
      b.setDate(date);
    }
  }

  public T read(DataReader dataReader) throws SQLException {

    Date ts = dataReader.getDate();
    if (ts == null) {
      return null;
    } else {
      return convertFromDate(ts);
    }
  }

  public String formatValue(T t) {
    Date date = convertToDate(t);
    return date.toString();
  }

  public T parse(String value) {
    Date date = Date.valueOf(value);
    return convertFromDate(date);
  }

  public T parseDateTime(long systemTimeMillis) {
    Date ts = new Date(systemTimeMillis);
    return convertFromDate(ts);
  }

  public boolean isDateTimeCapable() {
    return true;
  }

  @Override
  public Object jsonRead(JsonParser ctx, JsonToken event) throws IOException {
    if (JsonToken.VALUE_NUMBER_INT == event) {
      return parseDateTime(ctx.getLongValue());
    } else {
      return convertFromDate(Date.valueOf(ctx.getText()));
    }
  }

  public void jsonWrite(JsonGenerator ctx, String name, Object value) throws IOException {
    long millis = convertToMillis(value);
    ctx.writeNumberField(name, millis);
  }

  public Object readData(DataInput dataInput) throws IOException {
    if (!dataInput.readBoolean()) {
      return null;
    } else {
      long val = dataInput.readLong();
      Date date = new Date(val);
      return convertFromDate(date);
    }
  }

  @SuppressWarnings("unchecked")
  public void writeData(DataOutput dataOutput, Object v) throws IOException {

    T value = (T) v;
    if (value == null) {
      dataOutput.writeBoolean(false);
    } else {
      dataOutput.writeBoolean(true);
      Date date = convertToDate(value);
      dataOutput.writeLong(date.getTime());
    }
  }

}
