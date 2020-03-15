package com.steffbeard.totalwar.nations.exceptions;

import com.steffbeard.totalwar.nations.util.metadata.CustomDataFieldType;

public class InvalidMetadataTypeException extends NationsException {
    private static final long serialVersionUID = 2335936343233569066L;
    
    public InvalidMetadataTypeException(CustomDataFieldType type) {
        super("The given string for type " + type.getTypeName() + " is not valid!");
    }
}
