package hr.schteph.common.rest.autoproxy;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author scvitanovic
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ModelExample {
    private String property1;

    private Object property2;

    private boolean property3;
}
