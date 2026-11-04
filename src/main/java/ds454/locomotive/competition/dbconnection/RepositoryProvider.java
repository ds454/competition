package ds454.locomotive.competition.dbconnection;

import org.springframework.context.ApplicationContext;

public class RepositoryProvider {
    private static ApplicationContext context;

    public static void setContext(ApplicationContext ctx) {
        context = ctx;
    }

    public static <T> T getRepository(Class<T> repository) {
        if (context == null) {
            throw new IllegalStateException("Spring ApplicationContext has not been initialized yet!");
        }
        return context.getBean(repository);
    }
}
