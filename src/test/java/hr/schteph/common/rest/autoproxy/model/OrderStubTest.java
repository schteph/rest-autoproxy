package hr.schteph.common.rest.autoproxy.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.NullHandling;
import org.springframework.data.domain.Sort.Order;

/**
 * @author scvitanovic
 */
public class OrderStubTest {

    @Test
    public final void testToOrder_noIgnoreCase() throws Exception {
        Direction direction = Direction.ASC;
        String property = "property1";
        boolean ignoreCase = false;
        NullHandling nh = NullHandling.NULLS_FIRST;
        boolean ascending = false;

        OrderStub os = new OrderStub(direction, property, ignoreCase, nh, ascending);
        Order expected = new Order(direction, property, nh);

        Order res = os.toOrder();
        assertEquals(expected, res);
        assertEquals(res.getDirection(), direction);
        assertEquals(res.getNullHandling(), nh);
        assertEquals(res.getProperty(), property);
        // ascending is true, because of the direction, the ascending property in orderStub is ignored
        assertTrue(res.isAscending());
        assertEquals(ignoreCase, res.isIgnoreCase());

    }

    @Test
    public final void testToOrder() throws Exception {
        Direction direction = Direction.ASC;
        String property = "property1";
        boolean ignoreCase = true;
        NullHandling nh = NullHandling.NULLS_FIRST;
        boolean ascending = false;

        OrderStub os = new OrderStub(direction, property, ignoreCase, nh, ascending);
        Order expected = new Order(direction, property, nh).ignoreCase();

        Order res = os.toOrder();
        assertEquals(expected, res);
        assertEquals(res.getDirection(), direction);
        assertEquals(res.getNullHandling(), nh);
        assertEquals(res.getProperty(), property);
        // ascending is true, because of the direction, the ascending property in orderStub is ignored
        assertTrue(res.isAscending());
        assertEquals(ignoreCase, res.isIgnoreCase());

    }

    @Test
    public final void testToOrderList() throws Exception {
        Direction direction = Direction.ASC;
        String property = "property1";
        boolean ignoreCase = true;
        NullHandling nh = NullHandling.NULLS_FIRST;
        boolean ascending = false;

        OrderStub os = new OrderStub(direction, property, ignoreCase, nh, ascending);
        Order expected = new Order(direction, property, nh).ignoreCase();

        List<Order> resList = OrderStub.toOrderList(Arrays.asList(os));

        assertEquals(1, resList.size());
        Order res = resList.get(0);

        assertEquals(expected, res);
        assertEquals(res.getDirection(), direction);
        assertEquals(res.getNullHandling(), nh);
        assertEquals(res.getProperty(), property);
        // ascending is true, because of the direction, the ascending property in orderStub is ignored
        assertTrue(res.isAscending());
        assertEquals(ignoreCase, res.isIgnoreCase());
    }

}
