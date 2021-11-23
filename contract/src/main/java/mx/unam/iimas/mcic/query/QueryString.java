package mx.unam.iimas.mcic.query;

import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder(toBuilder = true)
public class QueryString {
    SelectorString selector = null;
}
