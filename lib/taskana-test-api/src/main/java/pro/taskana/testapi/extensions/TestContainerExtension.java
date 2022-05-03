package pro.taskana.testapi.extensions;

import static org.junit.platform.commons.support.AnnotationSupport.isAnnotated;
import static pro.taskana.testapi.DockerContainerCreator.createDataSource;
import static pro.taskana.testapi.DockerContainerCreator.createDockerContainer;
import static pro.taskana.testapi.util.ExtensionCommunicator.getClassLevelStore;
import static pro.taskana.testapi.util.ExtensionCommunicator.isTopLevelClass;

import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import javax.sql.DataSource;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.JdbcDatabaseContainer;

import pro.taskana.common.internal.configuration.DB;
import pro.taskana.testapi.CleanTaskanaContext;
import pro.taskana.testapi.TaskanaEngineConfigurationModifier;
import pro.taskana.testapi.WithServiceProvider;

public class TestContainerExtension implements AfterAllCallback, InvocationInterceptor {

  public static final String STORE_DATA_SOURCE = "datasource";
  public static final String STORE_CONTAINER = "container";
  public static final String STORE_SCHEMA_NAME = "schemaName";

  private static DataSource DATA_SOURCE;

  public static final DB DATABASE = retrieveDatabaseFromEnv();

  static {
    createDockerContainer(DATABASE)
        .ifPresentOrElse(
            container -> {
              container.start();
              DATA_SOURCE = createDataSource(container);
            },
            () -> DATA_SOURCE = createDataSourceForH2());
    System.out.println("Container setup");
  }

  @Override
  public <T> T interceptTestClassConstructor(
      Invocation<T> invocation,
      ReflectiveInvocationContext<Constructor<T>> invocationContext,
      ExtensionContext extensionContext)
      throws Throwable {
    Class<?> testClass = extensionContext.getRequiredTestClass();
    if (isTopLevelClass(testClass) || isAnnotated(testClass, CleanTaskanaContext.class)) {
      Store store = getClassLevelStore(extensionContext);
      store.put(STORE_DATA_SOURCE, DATA_SOURCE);
      store.put(STORE_SCHEMA_NAME, determineSchemaName());
    } else if (TaskanaEngineConfigurationModifier.class.isAssignableFrom(testClass)
        || isAnnotated(testClass, WithServiceProvider.class)) {
      // since the implementation of TaskanaEngineConfigurationModifier implies the generation of a
      // new TaskanaEngine, we have to copy the schema name and datasource from the enclosing class'
      // store to the testClass store.
      // This allows the following extensions to generate a new TaskanaEngine for the testClass.
      Store parentStore = getClassLevelStore(extensionContext, testClass.getEnclosingClass());
      Store store = getClassLevelStore(extensionContext);
      copyValue(TestContainerExtension.STORE_SCHEMA_NAME, parentStore, store);
      copyValue(TestContainerExtension.STORE_DATA_SOURCE, parentStore, store);
    }
    return invocation.proceed();
  }

  @Override
  public void afterAll(ExtensionContext context) throws SQLException {
    Class<?> testClass = context.getRequiredTestClass();
    if (isTopLevelClass(testClass) || isAnnotated(testClass, CleanTaskanaContext.class)) {
      Optional.ofNullable(getClassLevelStore(context).get(STORE_CONTAINER))
          .map(JdbcDatabaseContainer.class::cast)
          .ifPresent(GenericContainer::stop);
    }
  }

  private static void copyValue(String key, Store source, Store destination) {
    Object value = source.get(key);
    destination.put(key, value);
  }

  private static String determineSchemaName() {
    String uniqueId = "A" + UUID.randomUUID().toString().replace('-', '_');
    return DATABASE == DB.POSTGRES ? uniqueId.toLowerCase() : uniqueId;
  }

  private static DB retrieveDatabaseFromEnv() {
    String property = System.getenv("DB");
    DB db;
    try {
      db = DB.valueOf(property);
    } catch (Exception ex) {
      db = DB.H2;
    }
    return db;
  }

  private static DataSource createDataSourceForH2() {
    PooledDataSource ds =
        new PooledDataSource(
            Thread.currentThread().getContextClassLoader(),
            "org.h2.Driver",
            "jdbc:h2:mem:"
                + "taskana"
                + ";LOCK_MODE=0;"
                + "INIT=CREATE SCHEMA IF NOT EXISTS "
                + determineSchemaName()
                + "\\;"
                + "SET COLLATION DEFAULT_de_DE ",
            "sa",
            "sa");
    ds.setPoolTimeToWait(50);
    ds.forceCloseAll(); // otherwise, the MyBatis pool is not initialized correctly

    return ds;
  }
}
