package hr.schteph.common.rest.autoproxy.model;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.NullHandling;
import org.springframework.data.domain.Sort.Order;
import org.springframework.util.CollectionUtils;

/**
 * Used for deserialization of json page values since the standard spring PageImpl doesn't have a default constructor so
 * it's not suitable for jackson.
 *
 * @author scvitanovic
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderStub {

    private Direction direction;

    private String property;

    private boolean ignoreCase;

    private NullHandling nullHandling;

    /**
     * This property is ignored, whether it is ascending or descending depends on the direction. This exists only to be
     * compatible with the json of a spring Order.
     */
    private boolean ascending;

    public Order toOrder() {
        Order retVal = new Order(direction, property, nullHandling);
        if (this.ignoreCase) {
            retVal = retVal.ignoreCase();
        }
        return retVal;
    }

    public static List<Order> toOrderList(List<OrderStub> input) {
        if (CollectionUtils.isEmpty(input)) {
            return new ArrayList<Order>();
        }
        List<Order> retVal = new ArrayList<>(input.size());
        for (OrderStub orderStub : input) {
            retVal.add(orderStub.toOrder());
        }
        return retVal;
    }
}
