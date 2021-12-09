package mx.unam.iimas.mcic.election;

import java.time.LocalDate;

public class ElectionHelper {
    public static boolean isExpired(Election election) {
        LocalDate currentTime = LocalDate.now();
        return election.isClosed() || !(election.getStartDate().compareTo(currentTime) <= 0 && election.getEndDate().compareTo(currentTime) > 0);
    }
}
