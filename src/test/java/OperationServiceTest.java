import org.dimamir999.config.Config;
import org.dimamir999.dao.FileDao;
import org.dimamir999.model.KeyValue;
import org.dimamir999.service.OperationService;
import org.dimamir999.service.StringKeyValueConverter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Config.class)
public class OperationServiceTest {
    String key = "key";
    String value = "value";
    String dataFile = "data";
    String tempFile = "temp_data";
    String line = "key|-|value";
    KeyValue<String, String> keyValue;

    @Autowired
    OperationService operationService;
    @Mock private FileDao fileDao;
    @Mock private StringKeyValueConverter stringKeyValueConverter;

    @Before
    public void before() throws IOException {
        MockitoAnnotations.initMocks(this);
        operationService.setFileDao(fileDao);
        operationService.setStringKeyValueConverter(stringKeyValueConverter);

        keyValue = new KeyValue<>(key, value);
    }

    @Test
    public void testCreateOneElementExpectedNormalCreate() throws IOException {
        when(fileDao.read(dataFile)).thenReturn("someKey|-|someValue\nanyKey|-|anyValue\n");
        when(fileDao.read(tempFile)).thenReturn("newKey|-|newValue\n");
        when(stringKeyValueConverter.decode("someKey|-|someValue")).thenReturn(new KeyValue<>("someKey","someValue"));
        when(stringKeyValueConverter.decode("anyKey|-|anyValue")).thenReturn(new KeyValue<>("anyKey","anyValue"));
        when(stringKeyValueConverter.decode("newKey|-|newValue")).thenReturn(new KeyValue<>("newKey","newValue"));
        when(stringKeyValueConverter.encode(keyValue)).thenReturn(line);

        KeyValue result = operationService.create(keyValue);

        verify(fileDao).read(dataFile);
        verify(fileDao).read(tempFile);
        verify(fileDao).append(line, tempFile);
        verify(stringKeyValueConverter).decode("someKey|-|someValue");
        verify(stringKeyValueConverter).decode("anyKey|-|anyValue");
        verify(stringKeyValueConverter).decode("newKey|-|newValue");
        verify(stringKeyValueConverter).encode(keyValue);

        assertEquals(keyValue, result);
    }

    @Test
    public void testCreateOneElementWithOccupiedKeyInTempDataExpectedNoCreate() throws IOException {
        when(fileDao.read(dataFile)).thenReturn("someKey|-|someValue\nanyKey|-|anyValue\n");
        when(fileDao.read(tempFile)).thenReturn("newKey|-|newValue\n" + line + "\n");
        when(stringKeyValueConverter.decode("someKey|-|someValue")).thenReturn(new KeyValue<>("someKey","someValue"));
        when(stringKeyValueConverter.decode("anyKey|-|anyValue")).thenReturn(new KeyValue<>("anyKey","anyValue"));
        when(stringKeyValueConverter.decode("newKey|-|newValue")).thenReturn(new KeyValue<>("newKey","newValue"));
        when(stringKeyValueConverter.decode(line)).thenReturn(keyValue);

        KeyValue result = operationService.create(keyValue);

        verify(fileDao).read(dataFile);
        verify(fileDao).read(tempFile);
        verify(stringKeyValueConverter).decode("someKey|-|someValue");
        verify(stringKeyValueConverter).decode("anyKey|-|anyValue");
        verify(stringKeyValueConverter).decode("newKey|-|newValue");
        verify(stringKeyValueConverter).decode(line);

        assertEquals(null, result);
    }

    @Test
    public void testCreateOneElementWithOccupiedKeyInDataExpectedNoCreate() throws IOException {
        when(fileDao.read(dataFile)).thenReturn("someKey|-|someValue\nanyKey|-|anyValue\n" + line + "\n");
        when(stringKeyValueConverter.decode("someKey|-|someValue")).thenReturn(new KeyValue<>("someKey","someValue"));
        when(stringKeyValueConverter.decode("anyKey|-|anyValue")).thenReturn(new KeyValue<>("anyKey","anyValue"));
        when(stringKeyValueConverter.decode(line)).thenReturn(keyValue);

        KeyValue result = operationService.create(keyValue);

        verify(fileDao).read(dataFile);
        verify(stringKeyValueConverter).decode("someKey|-|someValue");
        verify(stringKeyValueConverter).decode("anyKey|-|anyValue");
        verify(stringKeyValueConverter).decode(line);

        assertEquals(null, result);
    }

    @Test
    public void testGetOneElementFromTempDataExpectedNormalGet() throws IOException {
        when(fileDao.read(dataFile)).thenReturn("someKey|-|someValue\nanyKey|-|anyValue\n");
        when(fileDao.read(tempFile)).thenReturn("newKey|-|newValue\n" + line + "\n");
        when(stringKeyValueConverter.decode("someKey|-|someValue")).thenReturn(new KeyValue<>("someKey","someValue"));
        when(stringKeyValueConverter.decode("anyKey|-|anyValue")).thenReturn(new KeyValue<>("anyKey","anyValue"));
        when(stringKeyValueConverter.decode("newKey|-|newValue")).thenReturn(new KeyValue<>("newKey","newValue"));
        when(stringKeyValueConverter.decode(line)).thenReturn(keyValue);

        KeyValue result = operationService.get(key);

        verify(fileDao).read(dataFile);
        verify(fileDao).read(tempFile);
        verify(stringKeyValueConverter).decode("someKey|-|someValue");
        verify(stringKeyValueConverter).decode("anyKey|-|anyValue");
        verify(stringKeyValueConverter).decode("newKey|-|newValue");
        verify(stringKeyValueConverter).decode(line);

        assertEquals(keyValue, result);
    }

    @Test
    public void testGetOneElementFromDataExpectedNormalGet() throws IOException {
        when(fileDao.read(dataFile)).thenReturn("someKey|-|someValue\nanyKey|-|anyValue\n" + line + "\n");
        when(stringKeyValueConverter.decode("someKey|-|someValue")).thenReturn(new KeyValue<>("someKey","someValue"));
        when(stringKeyValueConverter.decode("anyKey|-|anyValue")).thenReturn(new KeyValue<>("anyKey","anyValue"));
        when(stringKeyValueConverter.decode(line)).thenReturn(keyValue);

        KeyValue result = operationService.get(key);

        verify(fileDao).read(dataFile);
        verify(stringKeyValueConverter).decode("someKey|-|someValue");
        verify(stringKeyValueConverter).decode("anyKey|-|anyValue");
        verify(stringKeyValueConverter).decode(line);

        assertEquals(keyValue, result);
    }

    @Test
    public void testGetNonExistingElementExpectedNull() throws IOException {
        when(fileDao.read(dataFile)).thenReturn("someKey|-|someValue\nanyKey|-|anyValue\n");
        when(fileDao.read(tempFile)).thenReturn("newKey|-|newValue\n");
        when(stringKeyValueConverter.decode("someKey|-|someValue")).thenReturn(new KeyValue<>("someKey","someValue"));
        when(stringKeyValueConverter.decode("anyKey|-|anyValue")).thenReturn(new KeyValue<>("anyKey","anyValue"));
        when(stringKeyValueConverter.decode("newKey|-|newValue")).thenReturn(new KeyValue<>("newKey","newValue"));

        KeyValue result = operationService.get(key);

        verify(fileDao).read(dataFile);
        verify(fileDao).read(tempFile);
        verify(stringKeyValueConverter).decode("someKey|-|someValue");
        verify(stringKeyValueConverter).decode("anyKey|-|anyValue");
        verify(stringKeyValueConverter).decode("newKey|-|newValue");

        assertEquals(null, result);
    }

    @Test
    public void testUpdateOneElementInTempDataExpectedNormalUpdate() throws IOException {
        String newValue = "oldValue";
        KeyValue<String, String> oldKeyValue = new KeyValue<>(key, newValue);
        when(fileDao.read(dataFile)).thenReturn("someKey|-|someValue\nanyKey|-|anyValue\n");
        when(fileDao.read(tempFile)).thenReturn("newKey|-|newValue\nkey|-|oldValue\n");
        when(stringKeyValueConverter.decode("someKey|-|someValue")).thenReturn(new KeyValue<>("someKey","someValue"));
        when(stringKeyValueConverter.decode("anyKey|-|anyValue")).thenReturn(new KeyValue<>("anyKey","anyValue"));
        when(stringKeyValueConverter.decode("newKey|-|newValue")).thenReturn(new KeyValue<>("newKey","newValue"));
        when(stringKeyValueConverter.decode("key|-|oldValue")).thenReturn(new KeyValue<>("key","oldValue"));
        when(stringKeyValueConverter.encode(oldKeyValue)).thenReturn("key|-|oldValue");
        when(stringKeyValueConverter.encode(keyValue)).thenReturn(line);

        KeyValue result = operationService.update(keyValue);

        verify(fileDao).read(dataFile);
        verify(fileDao).read(tempFile);
        verify(fileDao).write("newKey|-|newValue\n" + line + "\n", tempFile);
        verify(stringKeyValueConverter).decode("someKey|-|someValue");
        verify(stringKeyValueConverter).decode("anyKey|-|anyValue");
        verify(stringKeyValueConverter).decode("newKey|-|newValue");
        verify(stringKeyValueConverter).encode(oldKeyValue);
        verify(stringKeyValueConverter).encode(keyValue);

        assertEquals(oldKeyValue, result);
    }

    @Test
    public void testUpdateOneElementInDataExpectedNormalUpdate() throws IOException {
        String newValue = "oldValue";
        KeyValue<String, String> oldKeyValue = new KeyValue<>(key, newValue);
        when(fileDao.read(dataFile)).thenReturn("someKey|-|someValue\nanyKey|-|anyValue\nkey|-|oldValue\n");
        when(stringKeyValueConverter.decode("someKey|-|someValue")).thenReturn(new KeyValue<>("someKey","someValue"));
        when(stringKeyValueConverter.decode("anyKey|-|anyValue")).thenReturn(new KeyValue<>("anyKey","anyValue"));
        when(stringKeyValueConverter.decode("key|-|oldValue")).thenReturn(new KeyValue<>("key","oldValue"));
        when(stringKeyValueConverter.encode(oldKeyValue)).thenReturn("key|-|oldValue");
        when(stringKeyValueConverter.encode(keyValue)).thenReturn(line);

        KeyValue result = operationService.update(keyValue);

        verify(fileDao).read(dataFile);
        verify(fileDao).write("someKey|-|someValue\nanyKey|-|anyValue\n" + line + "\n", dataFile);
        verify(stringKeyValueConverter).decode("someKey|-|someValue");
        verify(stringKeyValueConverter).decode("anyKey|-|anyValue");
        verify(stringKeyValueConverter).encode(oldKeyValue);
        verify(stringKeyValueConverter).encode(keyValue);

        assertEquals(oldKeyValue, result);
    }

    @Test
    public void testUpdateNonExistingElementExpectedNull() throws IOException {
        when(fileDao.read(dataFile)).thenReturn("someKey|-|someValue\nanyKey|-|anyValue\n");
        when(fileDao.read(tempFile)).thenReturn("newKey|-|newValue\n");
        when(stringKeyValueConverter.decode("someKey|-|someValue")).thenReturn(new KeyValue<>("someKey","someValue"));
        when(stringKeyValueConverter.decode("anyKey|-|anyValue")).thenReturn(new KeyValue<>("anyKey","anyValue"));
        when(stringKeyValueConverter.decode("newKey|-|newValue")).thenReturn(new KeyValue<>("newKey","newValue"));

        KeyValue result = operationService.update(keyValue);

        verify(fileDao).read(dataFile);
        verify(fileDao).read(tempFile);
        verify(stringKeyValueConverter).decode("someKey|-|someValue");
        verify(stringKeyValueConverter).decode("anyKey|-|anyValue");
        verify(stringKeyValueConverter).decode("newKey|-|newValue");

        assertEquals(null, result);
    }

    @Test
    public void testDeleteOneElementFromTempDataExpectedNormalDelete() throws IOException {
        when(fileDao.read(dataFile)).thenReturn("someKey|-|someValue\nanyKey|-|anyValue\n");
        when(fileDao.read(tempFile)).thenReturn("newKey|-|newValue\n" + line + "\n");
        when(stringKeyValueConverter.decode("someKey|-|someValue")).thenReturn(new KeyValue<>("someKey","someValue"));
        when(stringKeyValueConverter.decode("anyKey|-|anyValue")).thenReturn(new KeyValue<>("anyKey","anyValue"));
        when(stringKeyValueConverter.decode("newKey|-|newValue")).thenReturn(new KeyValue<>("newKey","newValue"));
        when(stringKeyValueConverter.decode(line)).thenReturn(keyValue);
        when(stringKeyValueConverter.encode(keyValue)).thenReturn(line);

        KeyValue result = operationService.delete(key);

        verify(fileDao).read(dataFile);
        verify(fileDao).read(tempFile);
        verify(fileDao).write("newKey|-|newValue\n", tempFile);
        verify(stringKeyValueConverter).decode("someKey|-|someValue");
        verify(stringKeyValueConverter).decode("anyKey|-|anyValue");
        verify(stringKeyValueConverter).decode("newKey|-|newValue");
        verify(stringKeyValueConverter).decode(line);
        verify(stringKeyValueConverter).encode(keyValue);

        assertEquals(keyValue, result);
    }

    @Test
    public void testDeleteOneElementFromDataExpectedNormalDelete() throws IOException {
        when(fileDao.read(dataFile)).thenReturn("someKey|-|someValue\nanyKey|-|anyValue\n" + line + "\n");
        when(stringKeyValueConverter.decode("someKey|-|someValue")).thenReturn(new KeyValue<>("someKey","someValue"));
        when(stringKeyValueConverter.decode("anyKey|-|anyValue")).thenReturn(new KeyValue<>("anyKey","anyValue"));
        when(stringKeyValueConverter.decode(line)).thenReturn(keyValue);
        when(stringKeyValueConverter.encode(keyValue)).thenReturn(line);

        KeyValue result = operationService.delete(key);

        verify(fileDao).read(dataFile);
        verify(fileDao).write("someKey|-|someValue\nanyKey|-|anyValue\n", dataFile);
        verify(stringKeyValueConverter).decode("someKey|-|someValue");
        verify(stringKeyValueConverter).decode("anyKey|-|anyValue");
        verify(stringKeyValueConverter).decode(line);
        verify(stringKeyValueConverter).encode(keyValue);

        assertEquals(keyValue, result);
    }

    @Test
    public void testDeleteOneElementKeyExpectedNoDelete() throws IOException {
        when(fileDao.read(dataFile)).thenReturn("someKey|-|someValue\nanyKey|-|anyValue\n");
        when(fileDao.read(tempFile)).thenReturn("newKey|-|newValue\n");
        when(stringKeyValueConverter.decode("someKey|-|someValue")).thenReturn(new KeyValue<>("someKey","someValue"));
        when(stringKeyValueConverter.decode("anyKey|-|anyValue")).thenReturn(new KeyValue<>("anyKey","anyValue"));
        when(stringKeyValueConverter.decode("newKey|-|newValue")).thenReturn(new KeyValue<>("newKey","newValue"));

        KeyValue result = operationService.delete(key);

        verify(fileDao).read(dataFile);
        verify(fileDao).read(tempFile);
        verify(stringKeyValueConverter).decode("someKey|-|someValue");
        verify(stringKeyValueConverter).decode("anyKey|-|anyValue");
        verify(stringKeyValueConverter).decode("newKey|-|newValue");

        assertEquals(null, result);
    }
}