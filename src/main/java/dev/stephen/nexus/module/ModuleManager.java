package dev.stephen.nexus.module;

import dev.stephen.nexus.Client;
import dev.stephen.nexus.module.modules.client.*;
import dev.stephen.nexus.module.modules.combat.*;
import dev.stephen.nexus.module.modules.ghost.*;
import dev.stephen.nexus.module.modules.movement.*;
import dev.stephen.nexus.module.modules.other.*;
import dev.stephen.nexus.module.modules.other.Timer;
import dev.stephen.nexus.module.modules.player.*;
import dev.stephen.nexus.module.modules.render.*;
import lombok.Getter;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Getter
public final class ModuleManager {
    private List<Module> modules = new ArrayList<>();
    private static final File MODULES_FILE = new File("enabled_modules.json");
    public <T extends Module> T getModule(Class<T> clazz) {
        for (Module module : modules) {
            if (clazz.isInstance(module)) {
                return clazz.cast(module);
            }
        }
        return null;
    }

    public ModuleManager() {
        addModules();
        loadEnabledModules();
    }

    public List<Module> getModulesInCategory(ModuleCategory moduleCategory) {
        List<Module> categoryModules = new ArrayList<>();

        for (Module module : modules) {
            if (module.getModuleCategory().equals(moduleCategory)) {
                categoryModules.add(module);
            }
        }

        return categoryModules;
    }

    public List<Module> getEnabledModules() {
        List<Module> enabled = new ArrayList<>();
        for (Module module : modules) {
            if (module.isEnabled()) {
                enabled.add(module);
            }
        }
        return enabled;
    }

    public void addModules() {
        // CLIENT
        add(new CrackedName());
        add(new RefreshClientData());
        add(new Theme());

        // COMBAT
        add(new AntiBot());
        add(new Backtrack());
        add(new Criticals());
        add(new InfiniteAura());
        add(new KillAura());
        add(new TargetStrafe());
        add(new TickBase());
        add(new Velocity());

        // GHOST
        add(new AimAssist());
        add(new AutoClicker());
        add(new LagRange());
        add(new TriggerBot());

        // MOVEMENT
        add(new AirStuck());
        add(new ClickTP());
        add(new ElytraFly());
        add(new Fly());
        add(new InvMove());
        add(new KeepSprint());
        add(new LongJump());
        add(new MoveFix());
        add(new NoSlow());
        add(new Speed());
        add(new Spider());
        add(new Sprint());

        // OTHER
        add(new AntiCheat());
        add(new Disabler());
        add(new FlagDetector());
        add(new NoRotate());
        add(new StaffDetector());
        add(new Timer());

        // PLAYER
        add(new AntiVoid());
        add(new BedAura());
        add(new Blink());
        add(new ChestStealer());
        add(new InventoryManager());
        add(new NoFall());
        add(new Regen());
        add(new Scaffold());

        // RENDER
        add(new Ambience());
        add(new Animations());
        add(new ESP());
        add(new Fullbright());
        add(new NoRender());
        add(new Notifications());
        add(new PostProcessing());
        add(new ClickGUI());
        add(new TargetHUD());
        add(new Interface());

        Client.INSTANCE.getEventManager().subscribe(this);
    }

    public void add(Module module) {
        modules.add(module);
    }

    public void saveEnabledModules() {
        List<String> enabledModuleNames = modules.stream()
                .filter(Module::isEnabled)
                .map(Module::getName)
                .collect(Collectors.toList());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(MODULES_FILE))) {
            for (String moduleName : enabledModuleNames) {
                writer.write(moduleName);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadEnabledModules() {
        if (!MODULES_FILE.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(MODULES_FILE))) {
            String moduleName;
            while ((moduleName = reader.readLine()) != null) {
                Module module = getModuleByName(moduleName);
                if (module != null) {
                    module.setEnabled(true);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Module getModuleByName(String name) {
        for (Module module : modules) {
            if (module.getName().equalsIgnoreCase(name)) {
                return module;
            }
        }
        return null;
    }
}
