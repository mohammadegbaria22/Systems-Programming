package bgu.spl.mics.application.objects;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Represents the status of a system component.
 * Possible statuses:
 * - UP: The component is operational.
 * - DOWN: The component is non-operational.
 * - ERROR: The component has encountered an error.
 */
public enum STATUS {
    UP, DOWN, ERROR
    }
