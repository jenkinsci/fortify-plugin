/*******************************************************************************
 * (c) Copyright 2019 Micro Focus or one of its affiliates. 
 * 
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * https://opensource.org/licenses/MIT
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.fortify.plugin.jenkins;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.encoders.SunPNGEncoderAdapter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.junit.Before;
import org.junit.Test;

public class ChartActionTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testCreateChart() throws Exception {
		int data[] = { 10, 11, 9, 12, 13, 14, 16 };
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		for (int i = 0; i < data.length; i++) {
			dataset.addValue(data[i], Integer.valueOf(0), new Integer(i));
		}

		JFreeChart chart = ChartAction.createChart(dataset, null, null);

		BufferedImage image = chart.createBufferedImage(400, 200);
		SunPNGEncoderAdapter png = new SunPNGEncoderAdapter();

		File tmp = File.createTempFile("test", ".png");
		FileOutputStream out = new FileOutputStream(tmp);
		png.encode(image, out);

		// no exception, that means ok
		System.out.println("PNG = " + tmp.getAbsolutePath());
	}
}
