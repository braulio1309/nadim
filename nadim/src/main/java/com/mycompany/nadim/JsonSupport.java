package com.mycompany.nadim;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class JsonSupport {
    private JsonSupport() {
    }

    static String stringify(Object value) {
        StringBuilder sb = new StringBuilder();
        appendValue(sb, value);
        return sb.toString();
    }

    static Object parse(String json) throws IOException {
        Parser parser = new Parser(json);
        Object value = parser.parseValue();
        parser.skipWhitespace();
        if (!parser.isEnd()) {
            throw new IOException("Unexpected trailing data in JSON");
        }
        return value;
    }

    private static void appendValue(StringBuilder sb, Object value) {
        if (value == null) {
            sb.append("null");
        } else if (value instanceof String) {
            appendString(sb, (String) value);
        } else if (value instanceof Number || value instanceof Boolean) {
            sb.append(value.toString());
        } else if (value instanceof Map) {
            appendObject(sb, (Map<?, ?>) value);
        } else if (value instanceof List) {
            appendArray(sb, (List<?>) value);
        } else {
            appendString(sb, value.toString());
        }
    }

    private static void appendObject(StringBuilder sb, Map<?, ?> map) {
        sb.append('{');
        boolean first = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            appendString(sb, String.valueOf(entry.getKey()));
            sb.append(':');
            appendValue(sb, entry.getValue());
        }
        sb.append('}');
    }

    private static void appendArray(StringBuilder sb, List<?> list) {
        sb.append('[');
        boolean first = true;
        for (Object value : list) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            appendValue(sb, value);
        }
        sb.append(']');
    }

    private static void appendString(StringBuilder sb, String value) {
        sb.append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"':
                case '\\':
                    sb.append('\\').append(c);
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                    break;
            }
        }
        sb.append('"');
    }

    private static final class Parser {
        private final String input;
        private int index;

        Parser(String input) {
            this.input = input == null ? "" : input;
            this.index = 0;
        }

        boolean isEnd() {
            return index >= input.length();
        }

        void skipWhitespace() {
            while (!isEnd()) {
                char c = input.charAt(index);
                if (c == ' ' || c == '\n' || c == '\r' || c == '\t') {
                    index++;
                } else {
                    break;
                }
            }
        }

        Object parseValue() throws IOException {
            skipWhitespace();
            if (isEnd()) {
                throw new IOException("Unexpected end of JSON input");
            }
            char c = input.charAt(index);
            switch (c) {
                case '{':
                    return parseObject();
                case '[':
                    return parseArray();
                case '"':
                    return parseString();
                case 't':
                case 'f':
                    return parseBoolean();
                case 'n':
                    return parseNull();
                default:
                    if (c == '-' || Character.isDigit(c)) {
                        return parseNumber();
                    }
                    throw new IOException("Unexpected character '" + c + "' in JSON");
            }
        }

        private Map<String, Object> parseObject() throws IOException {
            expect('{');
            skipWhitespace();
            Map<String, Object> map = new LinkedHashMap<>();
            if (peek('}')) {
                index++;
                return map;
            }
            while (true) {
                String key = parseString();
                skipWhitespace();
                expect(':');
                Object value = parseValue();
                map.put(key, value);
                skipWhitespace();
                if (peek(',')) {
                    index++;
                } else if (peek('}')) {
                    index++;
                    break;
                } else {
                    throw new IOException("Expected ',' or '}' in JSON object");
                }
                skipWhitespace();
            }
            return map;
        }

        private List<Object> parseArray() throws IOException {
            expect('[');
            skipWhitespace();
            List<Object> list = new ArrayList<>();
            if (peek(']')) {
                index++;
                return list;
            }
            while (true) {
                Object value = parseValue();
                list.add(value);
                skipWhitespace();
                if (peek(',')) {
                    index++;
                } else if (peek(']')) {
                    index++;
                    break;
                } else {
                    throw new IOException("Expected ',' or ']' in JSON array");
                }
                skipWhitespace();
            }
            return list;
        }

        private String parseString() throws IOException {
            expect('"');
            StringBuilder sb = new StringBuilder();
            while (!isEnd()) {
                char c = input.charAt(index++);
                if (c == '"') {
                    break;
                }
                if (c == '\\') {
                    if (isEnd()) {
                        throw new IOException("Invalid escape sequence in JSON string");
                    }
                    char esc = input.charAt(index++);
                    switch (esc) {
                        case '"':
                        case '\\':
                        case '/':
                            sb.append(esc);
                            break;
                        case 'b':
                            sb.append('\b');
                            break;
                        case 'f':
                            sb.append('\f');
                            break;
                        case 'n':
                            sb.append('\n');
                            break;
                        case 'r':
                            sb.append('\r');
                            break;
                        case 't':
                            sb.append('\t');
                            break;
                        case 'u':
                            if (index + 4 > input.length()) {
                                throw new IOException("Invalid unicode escape in JSON string");
                            }
                            String hex = input.substring(index, index + 4);
                            index += 4;
                            try {
                                int code = Integer.parseInt(hex, 16);
                                sb.append((char) code);
                            } catch (NumberFormatException ex) {
                                throw new IOException("Invalid unicode escape in JSON string", ex);
                            }
                            break;
                        default:
                            throw new IOException("Invalid escape character '" + esc + "' in JSON string");
                    }
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }

        private Boolean parseBoolean() throws IOException {
            if (match("true")) {
                return Boolean.TRUE;
            }
            if (match("false")) {
                return Boolean.FALSE;
            }
            throw new IOException("Invalid boolean literal in JSON");
        }

        private Object parseNull() throws IOException {
            if (match("null")) {
                return null;
            }
            throw new IOException("Invalid null literal in JSON");
        }

        private Number parseNumber() throws IOException {
            int start = index;
            if (peek('-')) {
                index++;
            }
            while (!isEnd() && Character.isDigit(input.charAt(index))) {
                index++;
            }
            if (!isEnd() && input.charAt(index) == '.') {
                index++;
                while (!isEnd() && Character.isDigit(input.charAt(index))) {
                    index++;
                }
            }
            if (!isEnd() && (input.charAt(index) == 'e' || input.charAt(index) == 'E')) {
                index++;
                if (!isEnd() && (input.charAt(index) == '+' || input.charAt(index) == '-')) {
                    index++;
                }
                while (!isEnd() && Character.isDigit(input.charAt(index))) {
                    index++;
                }
            }
            String numberText = input.substring(start, index);
            try {
                if (numberText.contains(".") || numberText.contains("e") || numberText.contains("E")) {
                    return Double.parseDouble(numberText);
                }
                return Long.parseLong(numberText);
            } catch (NumberFormatException ex) {
                throw new IOException("Invalid number literal in JSON", ex);
            }
        }

        private boolean match(String literal) {
            if (input.startsWith(literal, index)) {
                index += literal.length();
                return true;
            }
            return false;
        }

        private void expect(char expected) throws IOException {
            if (isEnd() || input.charAt(index) != expected) {
                throw new IOException("Expected '" + expected + "' in JSON");
            }
            index++;
        }

        private boolean peek(char expected) {
            return !isEnd() && input.charAt(index) == expected;
        }
    }
}
