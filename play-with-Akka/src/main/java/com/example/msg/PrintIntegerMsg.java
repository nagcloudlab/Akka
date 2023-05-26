package com.example.msg;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PrintIntegerMsg implements PrintMsg{
    private int msg;
}
