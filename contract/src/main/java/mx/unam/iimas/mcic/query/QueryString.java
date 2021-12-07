package mx.unam.iimas.mcic.query;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class QueryString {
    SelectorString selector;
}
