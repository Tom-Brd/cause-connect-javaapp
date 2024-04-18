package org.pat.causeconnect.service.plugin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.pat.causeconnect.entity.Plugin;

import java.util.ArrayList;

@Getter
@AllArgsConstructor
public class PluginsResponse {
    private ArrayList<Plugin> plugins;
}
