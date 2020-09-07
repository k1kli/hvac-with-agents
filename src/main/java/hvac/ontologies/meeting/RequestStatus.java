package hvac.ontologies.meeting;

import java.util.HashMap;
import java.util.Map;

public enum RequestStatus {
    CANCELLED(0),
    ESTIMATION(1),
    EXECUTE(2),
    FAILED(3),
    FINISHED(4),
    FORECAST(5),
    OFFER(6),
    RESERVED(7),
    ;

    private final int value;
    private static final Map<Integer,RequestStatus> map = new HashMap<>();

    RequestStatus(int value) {
        this.value = value;
    }

    static {
        for (RequestStatus status : RequestStatus.values()) {
            map.put(status.value, status);
        }
    }

    public static RequestStatus valueOf(int status) {
        return map.get(status);
    }

    public int getValue() {
        return value;
    }
}
