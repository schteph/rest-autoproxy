package hr.schteph.common.rest.autoproxy.model;

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

    private Sort sort;

    private boolean first;

    private boolean last;

    public Page<T> toPage() {
        PageImpl<T> retVal;
        if (size < 1) {
            retVal = new PageImpl<>(content);
        } else {
            PageRequest pr = new PageRequest(number, size);
            retVal = new PageImpl<>(content, pr, totalElements);
        }

        return retVal;
    }
}