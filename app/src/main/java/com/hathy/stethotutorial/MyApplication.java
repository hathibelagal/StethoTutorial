package com.hathy.stethotutorial;

import android.app.Application;

import com.facebook.stetho.DumperPluginsProvider;
import com.facebook.stetho.Stetho;
import com.facebook.stetho.dumpapp.DumpException;
import com.facebook.stetho.dumpapp.DumperContext;
import com.facebook.stetho.dumpapp.DumperPlugin;

import java.util.ArrayList;
import java.util.Arrays;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Stetho.InitializerBuilder initializerBuilder = Stetho.newInitializerBuilder(this);
        initializerBuilder.enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this));

		// initializerBuilder.enableDumpapp(Stetho.defaultDumperPluginsProvider(context));

        initializerBuilder.enableDumpapp(new MyDumperPluginProvider());

        Stetho.Initializer initializer = initializerBuilder.build();
        Stetho.initialize(initializer);
    }

    class MyDumperPlugin implements DumperPlugin {
        @Override
        public String getName() {
            return "my_plugin";
        }

        @Override
        public void dump(DumperContext dumpContext) throws DumpException {

            // Print the package name of the app
            dumpContext.getStdout().println(MyApplication.this.getPackageName());
        }
    }

    class MyDumperPluginProvider implements DumperPluginsProvider {
        @Override
        public Iterable<DumperPlugin> get() {
            ArrayList<DumperPlugin> plugins = new ArrayList<>();

            // Add default plugins to retain original functionality
            for(DumperPlugin plugin:Stetho.defaultDumperPluginsProvider(MyApplication.this).get()){
                plugins.add(plugin);
            }

            // Add custom plugin
            plugins.add(new MyDumperPlugin());
            return plugins;
        }
    }
}
