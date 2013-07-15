package util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import se.sics.kompics.ComponentDefinition;

public class DynamicComponentLoader extends ComponentDefinition {
	public static  Class<? extends ComponentDefinition> loadComponent(URL path, String componentName) throws FileNotFoundException, IOException, URISyntaxException, ClassNotFoundException{
		JarInJarClassLoader jijLoader = new JarInJarClassLoader(path);
		jijLoader.searchAndAddNestedJars();
		@SuppressWarnings("unchecked")
		Class<? extends ComponentDefinition> newClass = (Class<? extends ComponentDefinition>) jijLoader.findClass(componentName);
		return newClass;
	}
}
