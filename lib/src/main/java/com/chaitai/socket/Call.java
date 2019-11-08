package com.chaitai.socket;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Call {
    Request request;
    Set<Callback> callback = new HashSet<>();
    String response;
}
