package cc.makepower.cc_door_face.utils;

import java.util.Map;

public class Map2JsonTool {

    /**
     * map 转json
     * @param objectMap
     * @return
     */
    public static String map2Json(Map<String, Object> objectMap) {

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("{");
        for (String key : objectMap.keySet()
        ) {
            Object value = objectMap.get(key);
            stringBuffer.append("\"");
            stringBuffer.append(key);
            stringBuffer.append("\"");
            stringBuffer.append(":");
            if (value instanceof String) {
                stringBuffer.append("\"");
                stringBuffer.append(value);
                stringBuffer.append("\"");
            } else {
                stringBuffer.append(value);
            }
            stringBuffer.append(",");
        }
        stringBuffer.deleteCharAt(stringBuffer.length()-1);

        stringBuffer.append("}");
        return stringBuffer.toString();
    }
}
