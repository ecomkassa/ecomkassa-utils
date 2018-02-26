package com.thepointmoscow.frws.umka;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
class FiscalProperty {
    private int tag;
    private Object value;
    private List<FiscalProperty> fiscprops;
}
