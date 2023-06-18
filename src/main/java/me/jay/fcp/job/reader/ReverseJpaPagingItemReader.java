package me.jay.fcp.job.reader;

import com.google.common.collect.Lists;
import org.springframework.batch.core.annotation.AfterRead;
import org.springframework.batch.core.annotation.BeforeRead;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class ReverseJpaPagingItemReader<T> extends ItemStreamSupport implements ItemReader<T> {

    private static final int DEFAULT_PAGE_SIZE = 100;

    private int page = 0;
    private int totalPage = 0;
    private List<T> readRows = Lists.newArrayList();

    private int pageSize = DEFAULT_PAGE_SIZE;
    private Function<Pageable, Page<T>> query;
    private Sort sort = Sort.unsorted();

    public ReverseJpaPagingItemReader() {
    }

    public void setPageSize (int pageSize) {
        this.pageSize = (pageSize > 0) ? pageSize : DEFAULT_PAGE_SIZE;
    }

    public void setQuery(Function<Pageable, Page<T>> query) {
        this.query = query;
    }

    public void setSort(Sort sort) {
        if (!Objects.isNull(sort)) {
            // pagination을 마지막 페이지부터 하기 때문에 sort direction을 모두 reverse로 설정한다.
            // ASC <-> DESC
            Iterator<Sort.Order> orderIterator = sort.iterator();
            final List<Sort.Order> reverseOrders = Lists.newLinkedList();
            while (orderIterator.hasNext()) {
                Sort.Order prev = orderIterator.next();
                reverseOrders.add(
                        new Sort.Order(
                                prev.getDirection().isAscending() ? Sort.Direction.DESC : Sort.Direction.ASC,
                                prev.getProperty()
                        )
                );
            }

            this.sort = Sort.by(reverseOrders);
        }
    }

    /**
     * 스텝 실행 전에 동작함
     */
    @BeforeStep
    public void beforeStep() {
        // 뒤에서부터 읽을 것이기 때문에 마지막 페이지 번호를 구해서 page에 넣어준다.
        totalPage = getTargetData(0).getTotalPages();
        page = totalPage - 1;
    }

    /**
     * read() 함수가 실행되기 전에 동작함
     */
    @BeforeRead
    public void beforeRead() {
        if (page < 0)
            return;

        // 읽은 데이터를 모두 소진하면 db로부터 데이터를 가져와서 채워넣는다.
        if (readRows.isEmpty())
            readRows = Lists.newArrayList(getTargetData(page).getContent());
    }

    @Override
    public T read() {
        // null 을 반환하면 Reader는 모든 데이터를 소진한 것으로 인지하고 종료한다.
        // 데이터를 리스트에서 거꾸로 (readRows.size() - 1) 뒤에서부터 빼준다.
        return readRows.isEmpty() ? null : readRows.remove(readRows.size() - 1);
    }

    /**
     * read() 이후에 동작함
     */
    @AfterRead
    public void afterRead() {
        // 데이터가 없다면 page를 1 차감한다.
        if (readRows.isEmpty())
            this.page --;
    }

    /**
     * page 번호에 해당하는 데이터를 가져와서 Page 형식으로 반환한다.
     */
    private Page<T> getTargetData(int readPage) {
        return Objects.isNull(query) ? Page.empty() : query.apply(PageRequest.of(readPage, pageSize, sort));
    }
}
