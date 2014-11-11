package com.avaje.ebean.json;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

/**
 * Utility that converts between JSON content and java Maps/Lists.
 */
public class EJson {

  /**
   * Write the nested Map/List as json.
   */
  public static String write(Object object) throws IOException {
    return EJsonWriter.write(object);
  }
  
  /**
   * Write the nested Map/List as json to the writer.
   */
  public static void write(Object object, Writer writer) throws IOException {
    EJsonWriter.write(object, writer);
  }
  
  /**
   * Write the nested Map/List as json to the jsonGenerator.
   */
  public static void write(Object object, JsonGenerator jsonGenerator) throws IOException {
    EJsonWriter.write(object, jsonGenerator);
  }
  
  /**
   * Parse the json and return as a Map.
   */
  public static Map<String,Object> parseObject(String json) throws IOException {
    return EJsonReader.parseObject(json);
  }
  
  /**
   * Parse the json and return as a Map taking a reader.
   */
  public static Map<String,Object> parseObject(Reader reader) throws IOException {
    return EJsonReader.parseObject(reader);
  }
  
  /**
   * Parse the json and return as a Map taking a JsonParser.
   */
  public static Map<String,Object> parseObject(JsonParser parser) throws IOException {
    return EJsonReader.parseObject(parser);
  }
  
  /**
   * Parse the json and return as a List.
   * @throws IOException 
   */
  public static List<Object> parseList(String json) throws IOException {
    return EJsonReader.parseList(json);
  }
  
  /**
   * Parse the json and return as a List taking a Reader.
   * @throws IOException 
   */
  public static List<Object> parseList(Reader reader) throws IOException {
    return EJsonReader.parseList(reader);
  }
  
  /**
   * Parse the json and return as a List taking a JsonParser.
   */
  public static List<Object> parseList(JsonParser parser) throws IOException {
    return EJsonReader.parseList(parser);
  }
  
  
  /**
   * Parse the json and return as a List or Map.
   */
  public static Object parse(String json) throws IOException {
    return EJsonReader.parse(json);
  }
  
  /**
   * Parse the json and return as a List or Map.
   */
  public static Object parse(Reader reader) throws IOException {
    return EJsonReader.parse(reader, false);
  }
  
  /**
   * Parse the json and return as a List or Map.
   */
  public static Object parse(JsonParser parser) throws IOException {
    return EJsonReader.parse(parser, false);
  }
  
//  /**
//   * Parse the json and return the next json value, List or Map.
//   * This will not consume all the reader content and return once the 
//   * next json object, list or value is read.
//   */
//  public static Object parsePartial(Reader reader) {
//    return EJsonReader.parse(reader, true);
//  }
  
  /**
   * Parse the json and return the next json value, List or Map.
   * This will not consume all the reader content and return once the 
   * next json object, list or value is read.
   */
  public static Object parsePartial(JsonParser parser) throws IOException {
    return EJsonReader.parse(parser, true);
  }
}
