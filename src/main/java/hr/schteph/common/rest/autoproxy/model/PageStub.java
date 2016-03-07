package hr.schteph.common.rest.autoproxy.model;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Used for deserialization of json page values since the standard spring PageImpl doesn't have a default constructor so
 * it's not suitable for jackson.
 *
 * @author scvitanovic
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageStub<T> {

    private int totalPages;

    private long totalElements;

    private int number;

    private int size;

    private int numberOfElements;

    private List<T> content;

    private List<OrderStub> sort;

    private boolean first;

    private boolean last;

    public Page<T> toPage() {
        PageImpl<T> retVal;
        if (size < 1) {
            retVal = new PageImpl<>(content);
        } else {
            PageRequest pr;
            if (!CollectionUtils.isEmpty(this.sort)) {
                Sort sort = new Sort(OrderStub.toOrderList(this.sort));
                pr = new PageRequest(number, size, sort);
            } else {
                pr = new PageRequest(number, size);
            }
            retVal = new PageImpl<>(content, pr, totalElements);
        }

        return retVal;
    }

    @SuppressWarnings("rawtypes")
    public Page toPage(JavaType pageElementType, ObjectMapper om) {
        PageImpl retVal;
        if (pageElementType == null) {
            return toPage();
        }
        List<T> lista = this.content;
        List<Object> pageElements = new ArrayList<>();
        for (T t : lista) {
            Object converted = om.convertValue(t, pageElementType);
            pageElements.add(converted);
        }
        if (size < 1) {
            retVal = new PageImpl<>(pageElements);
        } else {
            PageRequest pr;
            if (!CollectionUtils.isEmpty(this.sort)) {
                Sort sort = new Sort(OrderStub.toOrderList(this.sort));
                pr = new PageRequest(number, size, sort);
            } else {
                pr = new PageRequest(number, size);
            }
            retVal = new PageImpl<>(pageElements, pr, totalElements);
        }

        return retVal;
    }

}
