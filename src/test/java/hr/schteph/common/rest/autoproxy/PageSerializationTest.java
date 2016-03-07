package hr.schteph.common.rest.autoproxy;

import static org.junit.Assert.assertEquals;
import hr.schteph.common.rest.autoproxy.model.PageStub;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.SimpleType;

public class PageSerializationTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testPageSerialization_Model() throws Exception {
        List<ModelExample> list = new ArrayList<>();
        ObjectMapper om = new ObjectMapper();

        ModelExample me1 = new ModelExample("property1_1", "property2_1", true);
        ModelExample me2 = new ModelExample("property1_2", "property2_2", false);
        ModelExample me3 = new ModelExample("property1_3", "property2_3", true);
        list.add(me1);
        list.add(me2);
        list.add(me3);

        PageRequest pr = new PageRequest(0, 10, new Sort("property1", "property2"));
        Page<ModelExample> page = new PageImpl<>(list, pr, 3);
        String pageJson = om.writeValueAsString(page);
        System.out.println(pageJson);
        PageStub<ModelExample> ps = om.readValue(pageJson, PageStub.class);
        Page<ModelExample> res = ps.toPage(SimpleType.construct(ModelExample.class), om);
        assertEquals(3, res.getContent().size());
        List<ModelExample> lista = res.getContent();
        assertEquals(me1, lista.get(0));
        assertEquals(me2, lista.get(1));
        assertEquals(me3, lista.get(2));
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testPageSerialization_String() throws Exception {
        List<String> list = new ArrayList<>();
        ObjectMapper om = new ObjectMapper();

        list.add("first");
        list.add("second");
        list.add("third");

        PageRequest pr = new PageRequest(0, 10, new Sort("property1", "property2"));
        Page<String> page = new PageImpl<>(list, pr, 3);
        String pageJson = om.writeValueAsString(page);
        System.out.println(pageJson);
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
