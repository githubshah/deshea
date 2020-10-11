package video;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.bidimap.DualHashBidiMap;

public class MainS {
    public static void main(String[] args) {
        BidiMap conferenceMap = new DualHashBidiMap(); // receptionistIp => clientIp
        conferenceMap.put("59.89.53.93", "139.167.210.102");
        conferenceMap.put("59.89.53.9312", "139.167.210.10212");
        MapIterator mapIterator = conferenceMap.mapIterator();
        while (mapIterator.hasNext()) {
            String key = (String)mapIterator.next();
            String value = (String)mapIterator.getValue();
            System.out.println(key);
        }
    }
}
