/*******************************************************************************
 * Copyright (c) 2015 Federal Institute for Risk Assessment (BfR), Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Department Biological Safety - BfR
 *******************************************************************************/
package de.bund.bfr.knime.openkrise.views.canvas;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;

import javax.swing.Icon;

/**
 * This class has been automatically generated using svg2java
 * 
 */
public class FoodChainLabLogo implements Icon {

	private float origAlpha = 1.0f;

	/**
	 * Paints the transcoded SVG image on the specified graphics context. You
	 * can install a custom transformation on the graphics context to scale the
	 * image.
	 * 
	 * @param g
	 *            Graphics context.
	 */
	public void paint(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		origAlpha = 1.0f;
		Composite origComposite = g.getComposite();
		if (origComposite instanceof AlphaComposite) {
			AlphaComposite origAlphaComposite = (AlphaComposite) origComposite;
			if (origAlphaComposite.getRule() == AlphaComposite.SRC_OVER) {
				origAlpha = origAlphaComposite.getAlpha();
			}
		}

		// _0
		AffineTransform trans_0 = g.getTransform();
		paintRootGraphicsNode_0(g);
		g.setTransform(trans_0);

	}

	private void paintShapeNode_0_0_0(Graphics2D g) {
		GeneralPath shape0 = new GeneralPath();
		shape0.moveTo(136.92783, 5.9065394);
		shape0.lineTo(139.65106, 3.7286522);
		shape0.lineTo(177.78453, 47.699604);
		shape0.curveTo(180.48882, 45.029335, 181.70534, 42.31828, 182.83008, 38.624413);
		shape0.lineTo(194.2451, 68.427505);
		shape0.lineTo(166.20186, 53.15586);
		shape0.curveTo(170.32632, 52.722122, 172.76506, 51.714848, 174.9594, 50.016376);
		shape0.closePath();
		g.setPaint(new Color(0, 0, 0, 255));
		g.fill(shape0);
		g.setStroke(new BasicStroke(0.20000002f, 0, 0, 4.0f, null, 0.0f));
		g.draw(shape0);
	}

	private void paintShapeNode_0_0_1(Graphics2D g) {
		GeneralPath shape1 = new GeneralPath();
		shape1.moveTo(98.4309, 53.394676);
		shape1.lineTo(99.69409, 56.64484);
		shape1.lineTo(46.32699, 79.87323);
		shape1.curveTo(48.06568, 83.25264, 50.288486, 85.22467, 53.476532, 87.40321);
		shape1.lineTo(21.623245, 89.37698);
		shape1.lineTo(44.58671, 67.18857);
		shape1.curveTo(43.766384, 71.25383, 43.99774, 73.88223, 44.961758, 76.48427);
		shape1.closePath();
		g.fill(shape1);
		g.draw(shape1);
	}

	private void paintShapeNode_0_0_2(Graphics2D g) {
		GeneralPath shape2 = new GeneralPath();
		shape2.moveTo(97.68401, 54.253216);
		shape2.lineTo(95.88808, 57.242165);
		shape2.lineTo(44.843964, 29.276014);
		shape2.curveTo(43.237022, 32.720028, 43.03917, 35.68492, 43.26914, 39.539375);
		shape2.lineTo(22.202745, 15.565757);
		shape2.lineTo(53.806404, 20.132504);
		shape2.curveTo(50.09018, 21.973421, 48.153915, 23.76588, 46.687157, 26.121412);
		shape2.closePath();
		g.fill(shape2);
		g.draw(shape2);
	}

	private void paintShapeNode_0_0_3(Graphics2D g) {
		GeneralPath shape3 = new GeneralPath();
		shape3.moveTo(92.17194, 60.3726);
		shape3.lineTo(94.54836, 62.07096);
		shape3.lineTo(86.55017, 72.297935);
		shape3.curveTo(88.04435, 73.36192, 88.59966, 73.201515, 90.448814, 73.427345);
		shape3.lineTo(77.23551, 81.36407);
		shape3.lineTo(82.15823, 66.72679);
		shape3.curveTo(82.707924, 68.65471, 83.10947, 69.66006, 84.09936, 70.56267);
		shape3.closePath();
		g.fill(shape3);
		g.setStroke(new BasicStroke(0.19999999f, 0, 0, 4.0f, null, 0.0f));
		g.draw(shape3);
	}

	private void paintTextNode_0_0_4(Graphics2D g) {
		g.setComposite(AlphaComposite.getInstance(3, 1.0f * origAlpha));
		g.setComposite(AlphaComposite.getInstance(3, 1.0f * origAlpha));
		g.setComposite(AlphaComposite.getInstance(3, 1.0f * origAlpha));
		GeneralPath shape4 = new GeneralPath();
		shape4.moveTo(252.62086, 106.04543);
		shape4.lineTo(252.62086, 2.9670706);
		shape4.lineTo(322.18353, 2.9670706);
		shape4.lineTo(322.18353, 15.107723);
		shape4.lineTo(266.2615, 15.107723);
		shape4.lineTo(266.2615, 47.02967);
		shape4.lineTo(314.63663, 47.02967);
		shape4.lineTo(314.63663, 59.2172);
		shape4.lineTo(266.2615, 59.2172);
		shape4.lineTo(266.2615, 106.04543);
		shape4.lineTo(252.62086, 106.04543);
		shape4.closePath();
		shape4.moveTo(333.55072, 68.685974);
		shape4.quadTo(333.55072, 47.967175, 345.082, 37.982777);
		shape4.quadTo(354.73828, 29.685883, 368.56644, 29.685883);
		shape4.quadTo(383.98834, 29.685883, 393.7618, 39.76403);
		shape4.quadTo(403.53528, 49.84218, 403.53528, 67.65472);
		shape4.quadTo(403.53528, 82.04538, 399.1993, 90.31883);
		shape4.quadTo(394.86337, 98.59229, 386.61334, 103.16261);
		shape4.quadTo(378.36334, 107.73294, 368.56644, 107.73294);
		shape4.quadTo(352.91016, 107.73294, 343.23044, 97.67822);
		shape4.quadTo(333.55072, 87.62351, 333.55072, 68.685974);
		shape4.closePath();
		shape4.moveTo(346.582, 68.685974);
		shape4.quadTo(346.582, 83.029755, 352.83984, 90.17821);
		shape4.quadTo(359.09766, 97.32666, 368.56644, 97.32666);
		shape4.quadTo(377.98834, 97.32666, 384.24615, 90.15477);
		shape4.quadTo(390.504, 82.98288, 390.504, 68.2641);
		shape4.quadTo(390.504, 54.43594, 384.22272, 47.287487);
		shape4.quadTo(377.94147, 40.13903, 368.56644, 40.13903);
		shape4.quadTo(359.09766, 40.13903, 352.83984, 47.24061);
		shape4.quadTo(346.582, 54.34219, 346.582, 68.685974);
		shape4.closePath();
		shape4.moveTo(413.63684, 68.685974);
		shape4.quadTo(413.63684, 47.967175, 425.16812, 37.982777);
		shape4.quadTo(434.8244, 29.685883, 448.65256, 29.685883);
		shape4.quadTo(464.07446, 29.685883, 473.84793, 39.76403);
		shape4.quadTo(483.6214, 49.84218, 483.6214, 67.65472);
		shape4.quadTo(483.6214, 82.04538, 479.28543, 90.31883);
		shape4.quadTo(474.9495, 98.59229, 466.69946, 103.16261);
		shape4.quadTo(458.44946, 107.73294, 448.65256, 107.73294);
		shape4.quadTo(432.99628, 107.73294, 423.31656, 97.67822);
		shape4.quadTo(413.63684, 87.62351, 413.63684, 68.685974);
		shape4.closePath();
		shape4.moveTo(426.66812, 68.685974);
		shape4.quadTo(426.66812, 83.029755, 432.92596, 90.17821);
		shape4.quadTo(439.18378, 97.32666, 448.65256, 97.32666);
		shape4.quadTo(458.07446, 97.32666, 464.33228, 90.15477);
		shape4.quadTo(470.59012, 82.98288, 470.59012, 68.2641);
		shape4.quadTo(470.59012, 54.43594, 464.30884, 47.287487);
		shape4.quadTo(458.0276, 40.13903, 448.65256, 40.13903);
		shape4.quadTo(439.18378, 40.13903, 432.92596, 47.24061);
		shape4.quadTo(426.66812, 54.34219, 426.66812, 68.685974);
		shape4.closePath();
		shape4.moveTo(546.87933, 106.04543);
		shape4.lineTo(546.87933, 96.623535);
		shape4.quadTo(539.8012, 107.73294, 526.0199, 107.73294);
		shape4.quadTo(517.0668, 107.73294, 509.5902, 102.81105);
		shape4.quadTo(502.11362, 97.88917, 497.98862, 89.05321);
		shape4.quadTo(493.8636, 80.21725, 493.8636, 68.779724);
		shape4.quadTo(493.8636, 57.576572, 497.59018, 48.4828);
		shape4.quadTo(501.31674, 39.38903, 508.76987, 34.537457);
		shape4.quadTo(516.223, 29.685883, 525.4574, 29.685883);
		shape4.quadTo(532.20746, 29.685883, 537.4809, 32.521828);
		shape4.quadTo(542.75433, 35.35777, 546.0356, 39.95153);
		shape4.lineTo(546.0356, 2.9670706);
		shape4.lineTo(558.645, 2.9670706);
		shape4.lineTo(558.645, 106.04543);
		shape4.lineTo(546.87933, 106.04543);
		shape4.closePath();
		shape4.moveTo(506.89487, 68.779724);
		shape4.quadTo(506.89487, 83.123505, 512.9418, 90.22508);
		shape4.quadTo(518.98865, 97.32666, 527.1918, 97.32666);
		shape4.quadTo(535.4887, 97.32666, 541.3012, 90.52977);
		shape4.quadTo(547.1137, 83.73288, 547.1137, 69.810974);
		shape4.quadTo(547.1137, 54.482815, 541.20746, 47.310925);
		shape4.quadTo(535.3012, 40.13903, 526.6293, 40.13903);
		shape4.quadTo(518.1918, 40.13903, 512.54333, 47.02967);
		shape4.quadTo(506.89487, 53.920315, 506.89487, 68.779724);
		shape4.closePath();
		shape4.moveTo(653.68427, 69.904724);
		shape4.lineTo(667.32495, 73.32661);
		shape4.quadTo(663.0593, 90.15477, 651.903, 98.99073);
		shape4.quadTo(640.74677, 107.82669, 624.6686, 107.82669);
		shape4.quadTo(607.981, 107.82669, 597.55133, 101.02979);
		shape4.quadTo(587.12164, 94.2329, 581.66064, 81.36569);
		shape4.quadTo(576.1997, 68.498474, 576.1997, 53.73281);
		shape4.quadTo(576.1997, 37.607777, 582.36383, 25.631186);
		shape4.quadTo(588.5279, 13.654595, 599.87164, 7.4202056);
		shape4.quadTo(611.21545, 1.1858164, 624.8561, 1.1858164);
		shape4.quadTo(640.3249, 1.1858164, 650.87177, 9.060835);
		shape4.quadTo(661.41864, 16.935852, 665.5906, 31.232761);
		shape4.lineTo(652.1374, 34.373394);
		shape4.quadTo(648.5749, 23.123367, 641.7546, 17.990543);
		shape4.quadTo(634.9342, 12.857718, 624.5748, 12.857718);
		shape4.quadTo(612.71545, 12.857718, 604.7232, 18.553043);
		shape4.quadTo(596.731, 24.24837, 593.49664, 33.857765);
		shape4.quadTo(590.26227, 43.467163, 590.26227, 53.63906);
		shape4.quadTo(590.26227, 66.81097, 594.106, 76.607864);
		shape4.quadTo(597.94977, 86.40476, 606.0357, 91.25634);
		shape4.quadTo(614.1217, 96.10791, 623.5436, 96.10791);
		shape4.quadTo(634.9811, 96.10791, 642.92645, 89.49852);
		shape4.quadTo(650.87177, 82.88913, 653.68427, 69.904724);
		shape4.closePath();
		shape4.moveTo(682.5359, 106.04543);
		shape4.lineTo(682.5359, 2.9670706);
		shape4.lineTo(695.1922, 2.9670706);
		shape4.lineTo(695.1922, 39.95153);
		shape4.quadTo(704.0516, 29.685883, 717.55164, 29.685883);
		shape4.quadTo(725.8485, 29.685883, 731.9657, 32.943703);
		shape4.quadTo(738.0829, 36.201523, 740.70795, 41.96716);
		shape4.quadTo(743.33295, 47.7328, 743.33295, 58.701572);
		shape4.lineTo(743.33295, 106.04543);
		shape4.lineTo(730.67664, 106.04543);
		shape4.lineTo(730.67664, 58.701572);
		shape4.quadTo(730.67664, 49.232803, 726.5751, 44.896854);
		shape4.quadTo(722.4735, 40.560905, 714.92664, 40.560905);
		shape4.quadTo(709.3016, 40.560905, 704.35626, 43.4906);
		shape4.quadTo(699.41095, 46.420296, 697.3016, 51.412495);
		shape4.quadTo(695.1922, 56.404694, 695.1922, 65.17034);
		shape4.lineTo(695.1922, 106.04543);
		shape4.lineTo(682.5359, 106.04543);
		shape4.closePath();
		shape4.moveTo(811.3253, 96.811035);
		shape4.quadTo(804.29407, 102.81105, 797.8019, 105.271996);
		shape4.quadTo(791.30963, 107.73294, 783.8565, 107.73294);
		shape4.quadTo(771.5284, 107.73294, 764.91895, 101.70949);
		shape4.quadTo(758.3096, 95.686035, 758.3096, 86.35789);
		shape4.quadTo(758.3096, 80.8735, 760.8174, 76.326614);
		shape4.quadTo(763.3252, 71.77973, 767.3565, 69.03754);
		shape4.quadTo(771.38776, 66.29534, 776.45026, 64.88909);
		shape4.quadTo(780.20026, 63.904713, 787.70026, 63.014084);
		shape4.quadTo(803.02844, 61.185955, 810.29407, 58.654697);
		shape4.quadTo(810.34094, 56.029694, 810.34094, 55.326565);
		shape4.quadTo(810.34094, 47.592175, 806.77844, 44.45154);
		shape4.quadTo(801.90344, 40.13903, 792.3409, 40.13903);
		shape4.quadTo(783.43463, 40.13903, 779.169, 43.279663);
		shape4.quadTo(774.9034, 46.420296, 772.88776, 54.34219);
		shape4.lineTo(760.5127, 52.654686);
		shape4.quadTo(762.2002, 44.73279, 766.0674, 39.834343);
		shape4.quadTo(769.93463, 34.935894, 777.24713, 32.310886);
		shape4.quadTo(784.55963, 29.685883, 794.16907, 29.685883);
		shape4.quadTo(803.73157, 29.685883, 809.7081, 31.935886);
		shape4.quadTo(815.6847, 34.185894, 818.4972, 37.58434);
		shape4.quadTo(821.30975, 40.982784, 822.43475, 46.18592);
		shape4.quadTo(823.091, 49.420303, 823.091, 57.857822);
		shape4.lineTo(823.091, 74.732864);
		shape4.quadTo(823.091, 92.40478, 823.8879, 97.06885);
		shape4.quadTo(824.68475, 101.732925, 827.0754, 106.04543);
		shape4.lineTo(813.85657, 106.04543);
		shape4.quadTo(811.8878, 102.107925, 811.3253, 96.811035);
		shape4.closePath();
		shape4.moveTo(810.29407, 68.54535);
		shape4.quadTo(803.40344, 71.35786, 789.62213, 73.32661);
		shape4.quadTo(781.794, 74.45161, 778.55963, 75.857864);
		shape4.quadTo(775.32526, 77.264114, 773.56744, 79.98287);
		shape4.quadTo(771.80963, 82.70163, 771.80963, 85.98289);
		shape4.quadTo(771.80963, 91.045395, 775.6534, 94.4204);
		shape4.quadTo(779.49713, 97.79542, 786.8565, 97.79542);
		shape4.quadTo(794.16907, 97.79542, 799.8644, 94.60791);
		shape4.quadTo(805.5597, 91.4204, 808.23157, 85.84226);
		shape4.quadTo(810.29407, 81.57663, 810.29407, 73.18598);
		shape4.lineTo(810.29407, 68.54535);
		shape4.closePath();
		shape4.moveTo(842.75507, 17.498354);
		shape4.lineTo(842.75507, 2.9670706);
		shape4.lineTo(855.4114, 2.9670706);
		shape4.lineTo(855.4114, 17.498354);
		shape4.lineTo(842.75507, 17.498354);
		shape4.closePath();
		shape4.moveTo(842.75507, 106.04543);
		shape4.lineTo(842.75507, 31.373386);
		shape4.lineTo(855.4114, 31.373386);
		shape4.lineTo(855.4114, 106.04543);
		shape4.lineTo(842.75507, 106.04543);
		shape4.closePath();
		shape4.moveTo(874.70044, 106.04543);
		shape4.lineTo(874.70044, 31.373386);
		shape4.lineTo(886.0911, 31.373386);
		shape4.lineTo(886.0911, 41.96716);
		shape4.quadTo(894.29425, 29.685883, 909.8568, 29.685883);
		shape4.quadTo(916.6068, 29.685883, 922.25525, 32.09995);
		shape4.quadTo(927.9037, 34.51402, 930.7162, 38.451527);
		shape4.quadTo(933.52875, 42.389038, 934.65375, 47.82655);
		shape4.quadTo(935.3569, 51.342182, 935.3569, 60.107826);
		shape4.lineTo(935.3569, 106.04543);
		shape4.lineTo(922.70056, 106.04543);
		shape4.lineTo(922.70056, 60.623455);
		shape4.quadTo(922.70056, 52.88906, 921.224, 49.045303);
		shape4.quadTo(919.74744, 45.20154, 915.99744, 42.9281);
		shape4.quadTo(912.24744, 40.654655, 907.18494, 40.654655);
		shape4.quadTo(899.0755, 40.654655, 893.2161, 45.787483);
		shape4.quadTo(887.35675, 50.920307, 887.35675, 65.26409);
		shape4.lineTo(887.35675, 106.04543);
		shape4.lineTo(874.70044, 106.04543);
		shape4.closePath();
		shape4.moveTo(949.8647, 75.107864);
		shape4.lineTo(949.8647, 62.357834);
		shape4.lineTo(988.7242, 62.357834);
		shape4.lineTo(988.7242, 75.107864);
		shape4.lineTo(949.8647, 75.107864);
		shape4.closePath();
		shape4.moveTo(1003.77106, 106.04543);
		shape4.lineTo(1003.77106, 2.9670706);
		shape4.lineTo(1017.41174, 2.9670706);
		shape4.lineTo(1017.41174, 93.8579);
		shape4.lineTo(1068.1775, 93.8579);
		shape4.lineTo(1068.1775, 106.04543);
		shape4.lineTo(1003.77106, 106.04543);
		shape4.closePath();
		shape4.moveTo(1131.5292, 96.811035);
		shape4.quadTo(1124.4979, 102.81105, 1118.0057, 105.271996);
		shape4.quadTo(1111.5135, 107.73294, 1104.0604, 107.73294);
		shape4.quadTo(1091.7322, 107.73294, 1085.1228, 101.70949);
		shape4.quadTo(1078.5134, 95.686035, 1078.5134, 86.35789);
		shape4.quadTo(1078.5134, 80.8735, 1081.0212, 76.326614);
		shape4.quadTo(1083.529, 71.77973, 1087.5603, 69.03754);
		shape4.quadTo(1091.5916, 66.29534, 1096.654, 64.88909);
		shape4.quadTo(1100.4042, 63.904713, 1107.9042, 63.014084);
		shape4.quadTo(1123.2323, 61.185955, 1130.4979, 58.654697);
		shape4.quadTo(1130.5448, 56.029694, 1130.5448, 55.326565);
		shape4.quadTo(1130.5448, 47.592175, 1126.9823, 44.45154);
		shape4.quadTo(1122.1073, 40.13903, 1112.5448, 40.13903);
		shape4.quadTo(1103.6385, 40.13903, 1099.3728, 43.279663);
		shape4.quadTo(1095.1072, 46.420296, 1093.0916, 54.34219);
		shape4.lineTo(1080.7166, 52.654686);
		shape4.quadTo(1082.404, 44.73279, 1086.2712, 39.834343);
		shape4.quadTo(1090.1384, 34.935894, 1097.4509, 32.310886);
		shape4.quadTo(1104.7635, 29.685883, 1114.3729, 29.685883);
		shape4.quadTo(1123.9354, 29.685883, 1129.912, 31.935886);
		shape4.quadTo(1135.8885, 34.185894, 1138.701, 37.58434);
		shape4.quadTo(1141.5135, 40.982784, 1142.6385, 46.18592);
		shape4.quadTo(1143.2948, 49.420303, 1143.2948, 57.857822);
		shape4.lineTo(1143.2948, 74.732864);
		shape4.quadTo(1143.2948, 92.40478, 1144.0917, 97.06885);
		shape4.quadTo(1144.8885, 101.732925, 1147.2792, 106.04543);
		shape4.lineTo(1134.0604, 106.04543);
		shape4.quadTo(1132.0917, 102.107925, 1131.5292, 96.811035);
		shape4.closePath();
		shape4.moveTo(1130.4979, 68.54535);
		shape4.quadTo(1123.6073, 71.35786, 1109.826, 73.32661);
		shape4.quadTo(1101.9979, 74.45161, 1098.7634, 75.857864);
		shape4.quadTo(1095.529, 77.264114, 1093.7712, 79.98287);
		shape4.quadTo(1092.0134, 82.70163, 1092.0134, 85.98289);
		shape4.quadTo(1092.0134, 91.045395, 1095.8572, 94.4204);
		shape4.quadTo(1099.7009, 97.79542, 1107.0604, 97.79542);
		shape4.quadTo(1114.3729, 97.79542, 1120.0682, 94.60791);
		shape4.quadTo(1125.7635, 91.4204, 1128.4354, 85.84226);
		shape4.quadTo(1130.4979, 81.57663, 1130.4979, 73.18598);
		shape4.lineTo(1130.4979, 68.54535);
		shape4.closePath();
		shape4.moveTo(1174.5839, 106.04543);
		shape4.lineTo(1162.8182, 106.04543);
		shape4.lineTo(1162.8182, 2.9670706);
		shape4.lineTo(1175.4745, 2.9670706);
		shape4.lineTo(1175.4745, 39.717155);
		shape4.quadTo(1183.4902, 29.685883, 1195.959, 29.685883);
		shape4.quadTo(1202.8496, 29.685883, 1208.9902, 32.451515);
		shape4.quadTo(1215.1309, 35.217144, 1219.1152, 40.256218);
		shape4.quadTo(1223.0996, 45.29529, 1225.3496, 52.396873);
		shape4.quadTo(1227.5996, 59.49845, 1227.5996, 67.56097);
		shape4.quadTo(1227.5996, 86.77976, 1218.1074, 97.25635);
		shape4.quadTo(1208.6152, 107.73294, 1195.3027, 107.73294);
		shape4.quadTo(1182.084, 107.73294, 1174.5839, 96.67041);
		shape4.lineTo(1174.5839, 106.04543);
		shape4.closePath();
		shape4.moveTo(1174.4432, 68.123474);
		shape4.quadTo(1174.4432, 81.57663, 1178.0995, 87.52976);
		shape4.quadTo(1184.0527, 97.32666, 1194.2715, 97.32666);
		shape4.quadTo(1202.5684, 97.32666, 1208.6152, 90.107895);
		shape4.quadTo(1214.6621, 82.88913, 1214.6621, 68.6391);
		shape4.quadTo(1214.6621, 54.014065, 1208.8496, 47.05311);
		shape4.quadTo(1203.0371, 40.092155, 1194.834, 40.092155);
		shape4.quadTo(1186.5371, 40.092155, 1180.4902, 47.287487);
		shape4.quadTo(1174.4432, 54.482815, 1174.4432, 68.123474);
		shape4.closePath();
		g.setPaint(new Color(36, 73, 164, 255));
		g.fill(shape4);
		g.setComposite(AlphaComposite.getInstance(3, 1.0f * origAlpha));
	}

	private void paintShapeNode_0_0_5(Graphics2D g) {
		GeneralPath shape5 = new GeneralPath();
		shape5.moveTo(199.84685, 365.67957);
		shape5.curveTo(199.84685, 398.51535, 173.2282, 425.134, 140.39241, 425.134);
		shape5.curveTo(107.556625, 425.134, 80.937965, 398.51535, 80.937965, 365.67957);
		shape5.curveTo(80.937965, 332.84378, 107.556625, 306.22513, 140.39241, 306.22513);
		shape5.curveTo(173.2282, 306.22513, 199.84685, 332.84378, 199.84685, 365.67957);
		shape5.closePath();
		g.setPaint(new Color(230, 230, 230, 255));
		g.fill(shape5);
		g.setPaint(new Color(0, 0, 0, 255));
		g.setStroke(new BasicStroke(11.598472f, 0, 0, 4.0f, null, 0.0f));
		g.draw(shape5);
	}

	private void paintShapeNode_0_0_6(Graphics2D g) {
		GeneralPath shape6 = new GeneralPath();
		shape6.moveTo(199.84685, 365.67957);
		shape6.curveTo(199.84685, 398.51535, 173.2282, 425.134, 140.39241, 425.134);
		shape6.curveTo(107.556625, 425.134, 80.937965, 398.51535, 80.937965, 365.67957);
		shape6.curveTo(80.937965, 332.84378, 107.556625, 306.22513, 140.39241, 306.22513);
		shape6.curveTo(173.2282, 306.22513, 199.84685, 332.84378, 199.84685, 365.67957);
		shape6.closePath();
		g.setPaint(new Color(230, 230, 230, 255));
		g.fill(shape6);
		g.setPaint(new Color(0, 0, 0, 255));
		g.draw(shape6);
	}

	private void paintShapeNode_0_0_7(Graphics2D g) {
		GeneralPath shape7 = new GeneralPath();
		shape7.moveTo(199.84685, 365.67957);
		shape7.curveTo(199.84685, 398.51535, 173.2282, 425.134, 140.39241, 425.134);
		shape7.curveTo(107.556625, 425.134, 80.937965, 398.51535, 80.937965, 365.67957);
		shape7.curveTo(80.937965, 332.84378, 107.556625, 306.22513, 140.39241, 306.22513);
		shape7.curveTo(173.2282, 306.22513, 199.84685, 332.84378, 199.84685, 365.67957);
		shape7.closePath();
		g.setPaint(new Color(230, 230, 230, 255));
		g.fill(shape7);
		g.setPaint(new Color(0, 0, 0, 255));
		g.draw(shape7);
	}

	private void paintShapeNode_0_0_8(Graphics2D g) {
		GeneralPath shape8 = new GeneralPath();
		shape8.moveTo(199.84685, 365.67957);
		shape8.curveTo(199.84685, 398.51535, 173.2282, 425.134, 140.39241, 425.134);
		shape8.curveTo(107.556625, 425.134, 80.937965, 398.51535, 80.937965, 365.67957);
		shape8.curveTo(80.937965, 332.84378, 107.556625, 306.22513, 140.39241, 306.22513);
		shape8.curveTo(173.2282, 306.22513, 199.84685, 332.84378, 199.84685, 365.67957);
		shape8.closePath();
		g.setPaint(new Color(230, 230, 230, 255));
		g.fill(shape8);
		g.setPaint(new Color(0, 0, 0, 255));
		g.draw(shape8);
	}

	private void paintShapeNode_0_0_9(Graphics2D g) {
		GeneralPath shape9 = new GeneralPath();
		shape9.moveTo(199.84685, 365.67957);
		shape9.curveTo(199.84685, 398.51535, 173.2282, 425.134, 140.39241, 425.134);
		shape9.curveTo(107.556625, 425.134, 80.937965, 398.51535, 80.937965, 365.67957);
		shape9.curveTo(80.937965, 332.84378, 107.556625, 306.22513, 140.39241, 306.22513);
		shape9.curveTo(173.2282, 306.22513, 199.84685, 332.84378, 199.84685, 365.67957);
		shape9.closePath();
		g.setPaint(new Color(128, 128, 128, 255));
		g.fill(shape9);
		g.setPaint(new Color(0, 0, 0, 255));
		g.draw(shape9);
	}

	private void paintShapeNode_0_0_10(Graphics2D g) {
		GeneralPath shape10 = new GeneralPath();
		shape10.moveTo(199.84685, 365.67957);
		shape10.curveTo(199.84685, 398.51535, 173.2282, 425.134, 140.39241, 425.134);
		shape10.curveTo(107.556625, 425.134, 80.937965, 398.51535, 80.937965, 365.67957);
		shape10.curveTo(80.937965, 332.84378, 107.556625, 306.22513, 140.39241, 306.22513);
		shape10.curveTo(173.2282, 306.22513, 199.84685, 332.84378, 199.84685, 365.67957);
		shape10.closePath();
		g.setPaint(new Color(128, 128, 128, 255));
		g.fill(shape10);
		g.setPaint(new Color(0, 0, 0, 255));
		g.draw(shape10);
	}

	private void paintShapeNode_0_0_11(Graphics2D g) {
		GeneralPath shape11 = new GeneralPath();
		shape11.moveTo(101.79908, 52.63894);
		shape11.lineTo(99.47955, 50.035275);
		shape11.lineTo(114.45359, 37.131035);
		shape11.curveTo(111.64339, 34.572468, 108.87152, 33.501724, 105.12311, 32.57486);
		shape11.lineTo(134.27753, 19.592314);
		shape11.lineTo(120.5176, 48.407417);
		shape11.curveTo(119.86531, 44.311836, 118.72987, 41.930065, 116.9172, 39.829086);
		shape11.closePath();
		g.fill(shape11);
		g.setStroke(new BasicStroke(0.20000002f, 0, 0, 4.0f, null, 0.0f));
		g.draw(shape11);
	}

	private void paintShapeNode_0_0_12(Graphics2D g) {
		GeneralPath shape12 = new GeneralPath();
		shape12.moveTo(101.7846, 60.97994);
		shape12.lineTo(103.74752, 58.097893);
		shape12.lineTo(132.45154, 76.01947);
		shape12.curveTo(134.25163, 72.672356, 134.61768, 69.723495, 134.60713, 65.862206);
		shape12.lineTo(154.27692, 90.99439);
		shape12.lineTo(122.983894, 84.63882);
		shape12.curveTo(126.79875, 83.012085, 128.83376, 81.33257, 130.43202, 79.06421);
		shape12.closePath();
		g.fill(shape12);
		g.draw(shape12);
	}

	private void paintShapeNode_0_0_13(Graphics2D g) {
		GeneralPath shape13 = new GeneralPath();
		shape13.moveTo(199.84685, 365.67957);
		shape13.curveTo(199.84685, 398.51535, 173.2282, 425.134, 140.39241, 425.134);
		shape13.curveTo(107.556625, 425.134, 80.937965, 398.51535, 80.937965, 365.67957);
		shape13.curveTo(80.937965, 332.84378, 107.556625, 306.22513, 140.39241, 306.22513);
		shape13.curveTo(173.2282, 306.22513, 199.84685, 332.84378, 199.84685, 365.67957);
		shape13.closePath();
		g.setPaint(new Color(230, 230, 230, 255));
		g.fill(shape13);
		g.setPaint(new Color(0, 0, 0, 255));
		g.setStroke(new BasicStroke(11.598472f, 0, 0, 4.0f, null, 0.0f));
		g.draw(shape13);
	}

	private void paintCanvasGraphicsNode_0_0(Graphics2D g) {
		// _0_0_0
		AffineTransform trans_0_0_0 = g.getTransform();
		g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
		paintShapeNode_0_0_0(g);
		g.setTransform(trans_0_0_0);
		// _0_0_1
		AffineTransform trans_0_0_1 = g.getTransform();
		g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
		paintShapeNode_0_0_1(g);
		g.setTransform(trans_0_0_1);
		// _0_0_2
		AffineTransform trans_0_0_2 = g.getTransform();
		g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
		paintShapeNode_0_0_2(g);
		g.setTransform(trans_0_0_2);
		// _0_0_3
		AffineTransform trans_0_0_3 = g.getTransform();
		g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
		paintShapeNode_0_0_3(g);
		g.setTransform(trans_0_0_3);
		// _0_0_4
		AffineTransform trans_0_0_4 = g.getTransform();
		g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
		paintTextNode_0_0_4(g);
		g.setTransform(trans_0_0_4);
		// _0_0_5
		AffineTransform trans_0_0_5 = g.getTransform();
		g.transform(new AffineTransform(0.1682351529598236f, 0.0f, 0.0f, 0.1682351529598236f,
				-12.638666152954102f, 35.61139678955078f));
		paintShapeNode_0_0_5(g);
		g.setTransform(trans_0_0_5);
		// _0_0_6
		AffineTransform trans_0_0_6 = g.getTransform();
		g.transform(new AffineTransform(0.1682351529598236f, 0.0f, 0.0f, 0.1682351529598236f,
				47.6943473815918f, 31.508752822875977f));
		paintShapeNode_0_0_6(g);
		g.setTransform(trans_0_0_6);
		// _0_0_7
		AffineTransform trans_0_0_7 = g.getTransform();
		g.transform(new AffineTransform(0.1682351529598236f, 0.0f, 0.0f, 0.1682351529598236f,
				141.08985900878906f, 34.88740158081055f));
		paintShapeNode_0_0_7(g);
		g.setTransform(trans_0_0_7);
		// _0_0_8
		AffineTransform trans_0_0_8 = g.getTransform();
		g.transform(new AffineTransform(0.1682351529598236f, 0.0f, 0.0f, 0.1682351529598236f,
				119.36997985839844f, -50.30281448364258f));
		paintShapeNode_0_0_8(g);
		g.setTransform(trans_0_0_8);
		// _0_0_9
		AffineTransform trans_0_0_9 = g.getTransform();
		g.transform(new AffineTransform(0.1682351529598236f, 0.0f, 0.0f, 0.1682351529598236f,
				-11.914666175842285f, -50.54414749145508f));
		paintShapeNode_0_0_9(g);
		g.setTransform(trans_0_0_9);
		// _0_0_10
		AffineTransform trans_0_0_10 = g.getTransform();
		g.transform(new AffineTransform(0.1682351529598236f, 0.0f, 0.0f, 0.1682351529598236f,
				178.9476318359375f, 16.99625015258789f));
		paintShapeNode_0_0_10(g);
		g.setTransform(trans_0_0_10);
		// _0_0_11
		AffineTransform trans_0_0_11 = g.getTransform();
		g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
		paintShapeNode_0_0_11(g);
		g.setTransform(trans_0_0_11);
		// _0_0_12
		AffineTransform trans_0_0_12 = g.getTransform();
		g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
		paintShapeNode_0_0_12(g);
		g.setTransform(trans_0_0_12);
		// _0_0_13
		AffineTransform trans_0_0_13 = g.getTransform();
		g.transform(new AffineTransform(0.1682351529598236f, 0.0f, 0.0f, 0.1682351529598236f,
				73.03421783447266f, -6.380380630493164f));
		paintShapeNode_0_0_13(g);
		g.setTransform(trans_0_0_13);
	}

	private void paintRootGraphicsNode_0(Graphics2D g) {
		// _0_0
		g.setComposite(AlphaComposite.getInstance(3, 1.0f * origAlpha));
		AffineTransform trans_0_0 = g.getTransform();
		g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
		paintCanvasGraphicsNode_0_0(g);
		g.setTransform(trans_0_0);
	}

	/**
	 * Returns the X of the bounding box of the original SVG image.
	 * 
	 * @return The X of the bounding box of the original SVG image.
	 */
	public int getOrigX() {
		return 1;
	}

	/**
	 * Returns the Y of the bounding box of the original SVG image.
	 * 
	 * @return The Y of the bounding box of the original SVG image.
	 */
	public int getOrigY() {
		return 0;
	}

	/**
	 * Returns the width of the bounding box of the original SVG image.
	 * 
	 * @return The width of the bounding box of the original SVG image.
	 */
	public int getOrigWidth() {
		return 1228;
	}

	/**
	 * Returns the height of the bounding box of the original SVG image.
	 * 
	 * @return The height of the bounding box of the original SVG image.
	 */
	public int getOrigHeight() {
		return 109;
	}

	/**
	 * The current width of this resizable icon.
	 */
	int width;

	/**
	 * The current height of this resizable icon.
	 */
	int height;

	/**
	 * Creates a new transcoded SVG image.
	 */
	public FoodChainLabLogo() {
		this.width = getOrigWidth();
		this.height = getOrigHeight();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.Icon#getIconHeight()
	 */
	@Override
	public int getIconHeight() {
		return height;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.Icon#getIconWidth()
	 */
	@Override
	public int getIconWidth() {
		return width;
	}

	/*
	 * Set the dimension of the icon.
	 */

	public void setDimension(Dimension newDimension) {
		this.width = newDimension.width;
		this.height = newDimension.height;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics,
	 * int, int)
	 */
	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		Graphics2D g2d = (Graphics2D) g.create();
		g2d.translate(x, y);

		double coef1 = (double) this.width / (double) getOrigWidth();
		double coef2 = (double) this.height / (double) getOrigHeight();
		double coef = Math.min(coef1, coef2);
		g2d.scale(coef, coef);
		paint(g2d);
		g2d.dispose();
	}
}
