package ch.ethz.sis.afs.dto;

import lombok.Value;

@Value
public class Lock<O, E> {
    private O owner;
    private E resource;
    private LockType type;
}