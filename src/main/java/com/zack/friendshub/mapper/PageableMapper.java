package com.zack.friendshub.mapper;

import com.zack.friendshub.dto.response.PageableDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

@Component
public class PageableMapper {

    /**
     * Converts a PageableDto<S> to a PageableDto<T> using a mapping function for each element.
     *
     * @param source - the source PageableDto
     * @param mapper - a function to convert S -> T
     * @param <S>    - the type of elements in the source (e.g., User)
     * @param <T>    - the type of elements in the result (e.g., UserResponseDto)
     * @return a PageableDto<T> with the mapped content
     */
    public static <S, T> PageableDto<T> map(PageableDto<S> source, Function<S, T> mapper) {
        List<T> content = source.getContent()
                .stream()
                .map(mapper)
                .toList();

        return new PageableDto<>(
                content,
                source.getTotalElements(),
                source.getCurrentPage(),
                source.getTotalPages()
        );
    }
}
