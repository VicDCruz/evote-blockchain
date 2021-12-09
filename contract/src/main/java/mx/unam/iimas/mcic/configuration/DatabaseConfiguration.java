package mx.unam.iimas.mcic.configuration;

import org.apache.commons.lang3.ObjectUtils;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

public class DatabaseConfiguration {
    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        if (ObjectUtils.isEmpty(sessionFactory)) {
            try {
                // Create session based on hibernate.cfg.xml
                StandardServiceRegistry standardRegistry = new StandardServiceRegistryBuilder().configure().build();
                Metadata metaData = new MetadataSources(standardRegistry).getMetadataBuilder().build();
                return metaData.getSessionFactoryBuilder().build();
            } catch (Throwable e) {
                System.err.println("Error at creating sessionFactory: " + e);
                throw new ExceptionInInitializerError(e);
            }
        }
        return sessionFactory;
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}
