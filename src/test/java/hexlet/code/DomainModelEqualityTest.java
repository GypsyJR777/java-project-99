package hexlet.code;

import static org.assertj.core.api.Assertions.assertThat;

import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import java.lang.reflect.Field;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class DomainModelEqualityTest {

    @ParameterizedTest
    @MethodSource("domainModelClasses")
    void comparesEntitiesByPersistedId(Class<?> modelClass) throws Exception {
        var entity = newInstance(modelClass);
        var sameIdEntity = newInstance(modelClass);
        var anotherIdEntity = newInstance(modelClass);
        setId(entity, 1L);
        setId(sameIdEntity, 1L);
        setId(anotherIdEntity, 2L);

        assertThat(entity)
            .isEqualTo(entity)
            .isEqualTo(sameIdEntity)
            .isNotEqualTo(anotherIdEntity)
            .isNotEqualTo(new Object());
        assertThat(entity.hashCode()).isEqualTo(modelClass.hashCode());
    }

    @ParameterizedTest
    @MethodSource("domainModelClasses")
    void doesNotCompareNewEntitiesAsEqual(Class<?> modelClass) throws Exception {
        var entity = newInstance(modelClass);
        var anotherEntity = newInstance(modelClass);

        assertThat(entity).isNotEqualTo(anotherEntity);
    }

    private static Stream<Class<?>> domainModelClasses() {
        return Stream.of(
            Label.class,
            Task.class,
            TaskStatus.class,
            User.class
        );
    }

    private static Object newInstance(Class<?> modelClass) throws Exception {
        var constructor = modelClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    private static void setId(Object entity, Long id) throws Exception {
        Field field = entity.getClass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(entity, id);
    }
}
