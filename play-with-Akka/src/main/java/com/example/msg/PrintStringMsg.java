package com.example.msg;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PrintStringMsg implements PrintMsg{
    private String msg;
}
