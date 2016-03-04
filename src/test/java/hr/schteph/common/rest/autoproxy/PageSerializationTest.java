package hr.schteph.common.rest.autoproxy;

import static org.junit.Assert.assertEquals;
import hr.schteph.common.rest.autoproxy.model.PageStub;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.fasterxml.jackson.databind.ObjectMapper;

public class PageSerializationTest {

    @Test
    public void testPageSerialization() throws Exception {
        List<String> list = new ArrayList<>();
        ObjectMapper om = new ObjectMapper();

        list.add("first");
        list.add("second");
        list.add("third");

        PageRequest pr = new PageRequest(0, 10);
        Page<String> page = new PageImpl<>(list, pr, 3);
        String pageJson = om.writeValueAsString(page);
        PageStub ps = om.readValue(pageJson, PageStub.class);
        Page res = ps.toPage();
        assertEquals(page, res);

        page = new PageImpl<>(list);
        pageJson = om.writeValueAsString(page);
        ps = om.readValue(pageJson, PageStub.class);
        res = ps.toPage();
        assertEquals(page, res);
    }
}
