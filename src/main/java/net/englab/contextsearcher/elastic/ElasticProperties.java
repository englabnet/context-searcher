package net.englab.contextsearcher.elastic;

import co.elastic.clients.elasticsearch._types.mapping.IntegerNumberProperty;
import co.elastic.clients.elasticsearch._types.mapping.KeywordProperty;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TextProperty;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ElasticProperties {
    public static final Property NON_SEARCHABLE_TEXT_PROPERTY = TextProperty.of(b -> b.index(false))._toProperty();
    public static final Property TEXT_PROPERTY = TextProperty.of(b -> b)._toProperty();
    public static final Property KEYWORD_PROPERTY = KeywordProperty.of(b -> b)._toProperty();
    public static final Property INTEGER_PROPERTY = IntegerNumberProperty.of(b -> b)._toProperty();
}
