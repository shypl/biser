package org.shypl.biser.io;

import org.junit.Assert;
import org.junit.Test;

public class BiserUtilsTest {

	@Test
	public void testEncodeEntity() throws Exception {
		//arrange
		byte[] expected = {0};
		EntityStub data = new EntityStub();

		//act
		byte[] actual = BiserUtils.encodeEntity(data);

		//assert
		Assert.assertArrayEquals(expected, actual);
	}

	@Test
	public void testDecodeEntity() throws Exception {
		//arrange
		EntityStub expected = new EntityStub();
		byte[] data = {0};

		//act
		EntityStub actual = BiserUtils.decodeEntity(data, EntityStub.class);

		//assert
		Assert.assertEquals(expected, actual);
	}

	@Test
	@SuppressWarnings("ConstantConditions")
	public void testDecodeEntityNull() throws Exception {
		//arrange
		EntityStub expected = null;
		byte[] data = null;

		//act
		EntityStub actual = BiserUtils.decodeEntity(data, EntityStub.class);

		//assert
		Assert.assertEquals(expected, actual);
	}
}