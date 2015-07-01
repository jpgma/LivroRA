package br.edu.ifg.livroar.assets;

import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import br.edu.ifg.livroar.App;
import br.edu.ifg.livroar.scenes.Material;
import br.edu.ifg.livroar.scenes.Model;
import br.edu.ifg.livroar.scenes.Scene;
import br.edu.ifg.livroar.scenes.SceneObject;
import br.edu.ifg.livroar.scenes.animations.Animation;
import br.edu.ifg.livroar.scenes.animations.Keyframe;
import br.edu.ifg.livroar.util.RGBColor;
import br.edu.ifg.livroar.util.Vec2;
import br.edu.ifg.livroar.util.Vec3;

/**
 * Created by JoaoPaulo on 10/06/2015.
 */
public class ColladaLoader implements Asset3DLoader
{
	public static final String TAG = "ColladaLoader";
	private String filePath;
	private final Element mainElement;

	public ColladaLoader(String filePath) throws ParserConfigurationException, IOException, SAXException
	{
		this.filePath = filePath;
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document sceneDoc = dBuilder.parse(App.getContext().getAssets().open(filePath));
		sceneDoc.normalize();

		mainElement = (Element) sceneDoc.getElementsByTagName("COLLADA").item(0);
	}

	@Override
	public void loadObjects(Scene scene, String sceneName, String ... names)
	{
		Map<String, Element> libImages = getLibraryImagesMap();
		Map<String, Element> libEffects = getLibraryEffectsMap();
		Map<String, Element> libMaterials = getLibraryMaterialsMap();
		Map<String, Element> libGeoms = getLibraryGeometriesMap();
		Map<String, Element> libAnims = getLibraryAnimationsMap();
		Map<String, Element> sceneNodes = getSceneNodesMap(getLibraryVisualScenesMap().get(sceneName));

		if(names.length > 0) // Alguns objetos
		{
			for (String name : names)
			{
				Element curNode = sceneNodes.get(name);
				if (curNode != null)
				{
					scene.addObject(getSceneObject(scene,
					                               curNode,
					                               libImages,
					                               libEffects,
					                               libMaterials,
					                               libGeoms,
					                               libAnims));
				}
			}
		}
		else // Todos os objetos
		{
			Log.d(TAG, "Carregando todos os objetos da cena");
			for (String id : sceneNodes.keySet())
			{
				SceneObject o = getSceneObject(scene,
				                               sceneNodes.get(id),
				                               libImages,
				                               libEffects,
				                               libMaterials,
				                               libGeoms,
				                               libAnims);
				scene.addObject(o);
				Log.d(TAG, "Objeto '" + id + "' carregado: " + o.toString());
			}
		}
	}

	private Map<String, Element> getLibraryImagesMap()
	{
		Element libImages = (Element) mainElement.getElementsByTagName("library_images").item(0);
		if(libImages != null)
		{
			int imgCount = libImages.getElementsByTagName("image").getLength();
			Map<String, Element> map = new HashMap<>(imgCount);
			for (int i = 0; i < imgCount; i++)
			{
				String id = ((Element)libImages.getElementsByTagName("image").item(i)).getAttribute("id");
				map.put(id, (Element)libImages.getElementsByTagName("image").item(i));
			}
			return map;
		}

		return new HashMap<>(0);
	}

	private Map<String, Element> getLibraryEffectsMap()
	{
		Element libEffects = (Element) mainElement.getElementsByTagName("library_effects").item(0);
		if(libEffects != null)
		{
			int effCount = libEffects.getElementsByTagName("effect").getLength();
			Map<String, Element> map = new HashMap<>(effCount);
			for (int i = 0; i < effCount; i++)
			{
				String id = ((Element)libEffects.getElementsByTagName("effect").item(i)).getAttribute("id");
				map.put(id, (Element)libEffects.getElementsByTagName("effect").item(i));
			}
			return map;
		}

		return new HashMap<>(0);
	}

	private Map<String, Element> getLibraryMaterialsMap()
	{
		Element libMaterials = (Element) mainElement.getElementsByTagName("library_materials").item(0);
		if(libMaterials != null)
		{
			int matCount = libMaterials.getElementsByTagName("material").getLength();
			Map<String, Element> map = new HashMap<>(matCount);
			for (int i = 0; i < matCount; i++)
			{
				String id = ((Element)libMaterials.getElementsByTagName("material").item(i)).getAttribute("id");
				map.put(id, (Element)libMaterials.getElementsByTagName("material").item(i));
			}
			return map;
		}

		return new HashMap<>();
	}

	private Map<String, Element> getLibraryGeometriesMap()
	{
		Element libGeometries = (Element) mainElement.getElementsByTagName("library_geometries").item(0);
		if(libGeometries != null)
		{
			int geomCount = libGeometries.getElementsByTagName("geometry").getLength();
			Map<String, Element> map = new HashMap<>(geomCount);
			for (int i = 0; i < geomCount; i++)
			{
				String id = ((Element)libGeometries.getElementsByTagName("geometry").item(i)).getAttribute("id");
				map.put(id, (Element)libGeometries.getElementsByTagName("geometry").item(i));
			}
			return map;
		}

		return new HashMap<>(0);
	}

	private Map<String, Element> getLibraryAnimationsMap()
	{
		Element libAnims = (Element) mainElement.getElementsByTagName("library_animations").item(0);
		if(libAnims != null)
		{
			int animCount = libAnims.getElementsByTagName("animation").getLength();
			Map<String, Element> map = new HashMap<>(animCount);
			Element curElement;
			for (int i = 0; i < animCount; i++)
			{
				curElement = (Element) libAnims.getElementsByTagName("animation").item(i);
				String target = ((Element)curElement.getElementsByTagName("channel").item(0))
						.getAttribute("target");
				map.put(target, curElement);
			}
			return map;
		}
		return new HashMap<>(0);
	}

	private Map<String, Element> getLibraryVisualScenesMap()
	{
		Element libVScenes = (Element) mainElement.getElementsByTagName("library_visual_scenes").item(0);
		if(libVScenes != null)
		{
			int sceneCount = libVScenes.getElementsByTagName("visual_scene").getLength();
			Map<String, Element> map = new HashMap<>(sceneCount);
			for (int i = 0; i < sceneCount; i++)
			{
				String id = ((Element)libVScenes.getElementsByTagName("visual_scene").item(i)).getAttribute("id");
				map.put(id, (Element)libVScenes.getElementsByTagName("visual_scene").item(i));
			}
			return map;
		}

		return new HashMap<>(0);
	}

	private Map<String, Element> getSceneNodesMap(Element visualSceneElement)
	{
		int nodeCount = visualSceneElement.getElementsByTagName("node").getLength();
		Map<String, Element> map = new HashMap<>(nodeCount);
		for (int i = 0; i < nodeCount; i++)
		{
			String id = ((Element)visualSceneElement.getElementsByTagName("node").item(i)).getAttribute("id");
			map.put(id, (Element)visualSceneElement.getElementsByTagName("node").item(i));
		}
		return map;
	}

	private SceneObject getSceneObject(Scene s,
	                                   Element nodeElement,
	                                   Map<String, Element> libraryImages,
	                                   Map<String, Element> libraryEffects,
	                                   Map<String, Element> libraryMaterials,
	                                   Map<String, Element> libraryGeometries,
	                                   Map<String, Element> libraryAnimations)
	{
		SceneObject obj = new SceneObject();
		String nodeID = nodeElement.getAttribute("id");

		//Posicao
		nodeElement = (Element) nodeElement.getElementsByTagName("translate").item(0);
		if(nodeElement!=null)
		{
			String[] location = nodeElement.getTextContent().split("[ ]");
			obj.setLocation(new Vec3(Float.parseFloat(location[0]),
			                         Float.parseFloat(location[1]),
			                         Float.parseFloat(location[2])));
		}
		nodeElement = (Element) nodeElement.getParentNode();
		assert nodeElement != null;
		//Rotacao
		int count = nodeElement.getElementsByTagName("rotate").getLength();
		float x = 0, y = 0, z = 0;
		for (int i = 0; i < count; i++)
		{
			nodeElement = (Element) nodeElement.getElementsByTagName("rotate").item(i);
			if(nodeElement.getAttribute("sid").endsWith("X"))
				x = Float.parseFloat(nodeElement
						                     .getTextContent().split("[ ]")[3]);
			else if(nodeElement.getAttribute("sid").endsWith("Y"))
				y = Float.parseFloat(nodeElement
						                     .getTextContent().split("[ ]")[3]);
			else if(nodeElement.getAttribute("sid").endsWith("Z"))
				z = Float.parseFloat(nodeElement
						                     .getTextContent().split("[ ]")[3]);
			nodeElement = (Element) nodeElement.getParentNode();
		}
		obj.setRotation(new Vec3(x, y, z));
		nodeElement = (Element) nodeElement.getParentNode();
		assert nodeElement != null;
		//Escala
		nodeElement = (Element) nodeElement.getElementsByTagName("scale").item(0);
		if(nodeElement!=null)
		{
			String[] scaleS = nodeElement.getTextContent().split("[ ]");
			obj.setScale(new Vec3(Float.parseFloat(scaleS[0]) * 20,
			                      Float.parseFloat(scaleS[1]) * 20,
			                      Float.parseFloat(scaleS[2]) * 20));
		}
		nodeElement = (Element) nodeElement.getParentNode();
		assert nodeElement != null;

		//Geometry
		String geomId = null;
		nodeElement = (Element)nodeElement.getElementsByTagName("instance_geometry").item(0);
		if(nodeElement!=null)
			geomId = nodeElement.getAttribute("url").substring(1);

		Element meshElement = libraryGeometries.get(geomId);
		if(meshElement!=null)
		{
			Log.d(TAG, "Fazendo parse da geometry '" + geomId + "'" );
			meshElement = (Element) meshElement.getElementsByTagName("mesh").item(0);

			List<Vec3> rawPositions = new ArrayList<>();
			List<Vec3> positions;
			List<Vec3> rawNormals = new ArrayList<>();
			List<Vec3> normals;
			List<Vec2> rawUvs = new ArrayList<>();
			List<Vec2> uvs;
			List<RGBColor> rawColors = new ArrayList<>();
			List<RGBColor> colors;
			List<Model.Part> parts = new ArrayList<>();

			// SOURCES
			Element curSource;
			int sourceCount = meshElement.getElementsByTagName("source").getLength();
			Map<String, float[]> sources = new HashMap<>();
			for (int i = 0; i < sourceCount; i++) // Mapear sources
			{
				curSource = ((Element)meshElement.getElementsByTagName("source").item(i));
				String id = curSource.getAttribute("id");
				String[] srcS = curSource.getElementsByTagName("float_array").item(0).getTextContent().split("[ ]");
				float[] src = new float[srcS.length];
				for (int j = 0; j < srcS.length; j++)
					src[j] = Float.parseFloat(srcS[j]);
				sources.put(id, src);
			}
			Element vertsElement = (Element) meshElement.getElementsByTagName("vertices").item(0);
			String sourceID = ((Element)vertsElement.getElementsByTagName("input").item(0))
					.getAttribute("source").substring(1);
			float[] srcF = sources.get(sourceID);
			sources.remove(sourceID);
			for (int i = 0; i < srcF.length; i+=3) // add vertices
			{
				x = srcF[i];
				y = srcF[i+1];
				z = srcF[i+2];
				rawPositions.add(new Vec3(x,y,z));
			}

			// POLYLISTS
			Element curPolylist;
			List<String> semantics = new ArrayList<>();
			List<String> srcIds = new ArrayList<>();
			Map<Short, short[]> indexGroups = new HashMap<>(); // Indices secundarios(normals, uvs, colors, ...) mapeados ao indice de posicao
			Map<Short, List<Short>> matchingVerts = new HashMap<>();
			int polylistCount = meshElement.getElementsByTagName("polylist").getLength();
			for (int i = 0; i < polylistCount; i++)
			{
				curPolylist = (Element) meshElement.getElementsByTagName("polylist").item(i);

				//INDICES
				String[] indicesS = curPolylist.getElementsByTagName("p")
				                               .item(0).getTextContent().split("[ ]");
				short[] perInputIndices = new short[indicesS.length];
				for (int j = 0; j < indicesS.length; j++)
				{
					int indice = Integer.parseInt(indicesS[j]);
					if(indice <= Short.MAX_VALUE && indice >= 0)
						perInputIndices[j] = (short) indice;
					else
						perInputIndices[j] = Short.MAX_VALUE;
				}

				//INPUTS
				Element curInput;
				int inputCount = curPolylist.getElementsByTagName("input").getLength();
				semantics.clear(); // Semantics iguais para todos os polylists nas exportacoes do Blender
				for (int j = 0; j < inputCount; j++)
				{
					curInput = (Element) curPolylist.getElementsByTagName("input").item(j);
					String semantic = curInput.getAttribute("semantic");
					semantics.add(semantic);
					srcIds.add(curInput.getAttribute("source").substring(1));
				}

				//INDICES
				// processamento de vertices com mesmo indice de posicao para gerar indices e
				// listas de atributos apropriados
				short[] indices = new short[perInputIndices.length/inputCount];

				short curPrimary;
				short[] curSecondaries;
				for (int j = 0; j < perInputIndices.length; j+=inputCount)
				{
					curPrimary = perInputIndices[j]; //Indice de posicao
					curSecondaries = new short[inputCount - 1];
					for (int k = 0; k < inputCount-1; k++)
						curSecondaries[k] = perInputIndices[j+(k+1)]; //Demais indices

					if(indexGroups.containsKey(curPrimary)) //Testar indice de posicao contra indices previamente processados
					{
						List<Short> possibleMatches = new ArrayList<>();
						possibleMatches.add(curPrimary);

						if(matchingVerts.containsKey(curPrimary)) //Adicionar aos possiveis matches tds os previamente associados
						{
							for (short k : matchingVerts.get(curPrimary))
								possibleMatches.add(k);
						}

						boolean noMatch = true;
						for (short k : possibleMatches) //Testar possiveis matches
						{
							int matchCount = 0;
							for (int m = 0; m < curSecondaries.length; m++) //Comparar indices secundarios
							{
								if(curSecondaries[m] == indexGroups.get(k)[m])
									matchCount++;
							}

							if(matchCount==inputCount-1) // Vertice exatamente igual
							{
								noMatch = false;
								break;
							}
						}

						if(noMatch) //Vertice ainda nao indexado, duplicar posicao e associar indices
						{
							Vec3 pos = rawPositions.get(curPrimary);
							short newIndex = (short)rawPositions.size();

							if(!matchingVerts.containsKey(curPrimary))
								matchingVerts.put(curPrimary, new ArrayList<Short>());
							matchingVerts.get(curPrimary).add(newIndex);

							curPrimary = newIndex;
							rawPositions.add(pos);
						}
					}

					indexGroups.put(curPrimary, curSecondaries); // adicionando vertice processado
					indices[j/inputCount] = curPrimary;
				}

//				Log.d(TAG, "part indices count: " + indices.length);
//				for (int j = 0; j < indices.length; j++)
//					Log.d(TAG, "i " + indices[j]);

				Model.Part part = new Model.Part(indices);

				//MATERIAL
				String matName = curPolylist.getAttribute("material");
				int matId = s.addMaterial(getMaterial(libraryMaterials.get(matName), libraryImages, libraryEffects));
				part.setMaterialId(matId);

				parts.add(part);
			}

			//Populando listas de atributos baseado nos inputs do ultimo polylist,
			//o que deve ser igual a todos os outros na exportacao do blender
			for (int j = 0; j < semantics.size(); j++)
			{
				String srcID = srcIds.get(j);
				float[] src = sources.get(srcID);
				switch (semantics.get(j))
				{
					case "NORMAL":
						for (int k = 0; k < src.length; k+=3)
						{
							x = src[k];
							y = src[k+1];
							z = src[k+2];
							rawNormals.add(new Vec3(x,y,z));
						}
						break;
					case "TEXCOORD":
						for (int k = 0; k < src.length; k+=2)
						{
							x = src[k];
							y = src[k+1];
							rawUvs.add(new Vec2(x,y));
						}
						break;
					case "COLOR":
						for (int k = 0; k < src.length; k+=3)
						{
							float r = src[k];
							float g = src[k+1];
							float b = src[k+2];
							rawColors.add(new RGBColor(r,g,b));
						}
						break;
				}
			}

			positions = new ArrayList<>();
			normals = new ArrayList<>();
			uvs = new ArrayList<>();
			colors = new ArrayList<>();

			for (int j = 0; j < rawPositions.size(); j++)
			{
				positions.add(Vec3.ZERO);
				normals.add(Vec3.ZERO);
				uvs.add(Vec2.ZERO);
				colors.add(RGBColor.PINK);
			}

			//Populando listas finais de atributos do mesh de acordo com os
			// dados do processamento feito nos indices de cada polylist.
			for (short prim : indexGroups.keySet())
			{
				prim = (short) Math.abs(prim); // Forcando valor positivo
				Log.d(TAG, "prim: " + prim);
//				Log.d(TAG, "Pos " + prim + ": " + rawPositions.get(prim).toString());
				positions.set(prim, rawPositions.get(prim));
				short subIndex;
				for (int j = 0; j < indexGroups.get(prim).length; j++)
				{
					subIndex = indexGroups.get(prim)[j];
					switch (semantics.get(j+1))
					{
						case "NORMAL":
//							Log.d(TAG, "Norm " + prim + ": " + rawNormals.get(subIndex).toString());
							normals.set(prim, rawNormals.get(subIndex));
							break;
						case "TEXCOORD":
//							Log.d(TAG, "UV " + prim + ": " + rawUvs.get(subIndex).toString());
							uvs.set(prim, rawUvs.get(subIndex));
							break;
						case "COLOR":
//							Log.d(TAG, "Col " + prim + ": " + rawColors.get(subIndex).toString());
							colors.set(prim, rawColors.get(subIndex));
							break;
					}
				}
			}

			int meshId = s.addModel(new Model(positions, normals, uvs, colors, parts));
			obj.setMeshId(meshId);
//			Log.d(TAG, "Model count: " + s.getModels().size());
		}

		//Animations
//		obj.setAnimations(getAnimations(nodeID, libraryAnimations));

		return obj;
	}

	//Suportando apenas uma animacao locXYZ/rotXYZ/scaleXYZ por objeto como imposto pelo exporter do blender
	private List<Animation> getAnimations(String targetName, Map<String, Element> libraryAnimations)
	{
		List<Animation> animations = new ArrayList<>();
		List<Keyframe> locX, locY, locZ, rotX, rotY, rotZ, sclX, sclY, sclZ;
		List<Element> animslocRotScaleXYZ = new ArrayList<>(9);
		for (int i = 0; i < 9; i++)
			animslocRotScaleXYZ.add(null);
//        Element locX = null, locY = null, locZ = null,
//                rotX = null, rotY = null, rotZ = null,
//                scaleX = null, scaleY = null, scaleZ = null;

		for (String t : libraryAnimations.keySet())
		{
			Element curAnim = libraryAnimations.get(t);
			String[] target = t.split("[/]");

			if(target[0].equals(targetName))
			{
				switch (target[1])
				{
					case "location.X":
						animslocRotScaleXYZ.set(0,curAnim);
						break;
					case "location.Y":
						animslocRotScaleXYZ.set(1,curAnim);
						break;
					case "location.Z":
						animslocRotScaleXYZ.set(2,curAnim);
						break;
					case "rotationX.ANGLE":
						animslocRotScaleXYZ.set(3,curAnim);
						break;
					case "rotationY.ANGLE":
						animslocRotScaleXYZ.set(4,curAnim);
						break;
					case "rotationZ.ANGLE":
						animslocRotScaleXYZ.set(5,curAnim);
						break;
					case "scale.X":
						animslocRotScaleXYZ.set(6,curAnim);
						break;
					case "scale.Y":
						animslocRotScaleXYZ.set(7,curAnim);
						break;
					case "scale.Z":
						animslocRotScaleXYZ.set(8,curAnim);
				}
			}
		}

		for (int i = 0; i < 9; i++) // Parse anims
		{
			Map<String, String[]> sourcesString = new HashMap<>();

			Element curAnim = animslocRotScaleXYZ.get(i);
			if(curAnim!=null)
			{
				int sourceCount = curAnim.getElementsByTagName("source").getLength();
				for (int j = 0; j < sourceCount; j++)
				{
					curAnim = (Element) curAnim.getElementsByTagName("source").item(j);
					String id = curAnim.getAttribute("id");
					String type = ((Element)((Element)((Element)curAnim.getElementsByTagName("technique_common"))
							.getElementsByTagName("accessor")).getElementsByTagName("param")).getAttribute("type");
					switch (type)
					{
						case "float":
							sourcesString.put(id, curAnim.getElementsByTagName("float_array").item(0).getTextContent().split("[ ]"));
							break;
						case "name":
							sourcesString.put(id, curAnim.getElementsByTagName("Name_array").item(0).getTextContent().split("[ ]"));
							break;
					}
					curAnim = (Element) curAnim.getParentNode();
				}

				float[] input = null;
				float[] output = null;
				Vec2[] intangents = null;
				Vec2[] outtangents = null;
				String methodUrl = null;

				int inputCount = curAnim.getElementsByTagName("input").getLength();
				for (int j = 0; j < inputCount; j++)
				{
					curAnim = (Element) curAnim.getElementsByTagName("input").item(j);
					String semantic = curAnim.getAttribute("semantic");
					String srcUrl = curAnim.getAttribute("source").substring(1);
					switch (semantic)
					{
						case "INPUT":
							input = new float[sourcesString.get(srcUrl).length];
							for (int k = 0; k < input.length; k++)
								input[k] = Float.parseFloat(sourcesString.get(srcUrl)[k]);
							break;
						case "OUTPUT":
							output = new float[sourcesString.get(srcUrl).length];
							for (int k = 0; k < output.length; k++)
								output[k] = Float.parseFloat(sourcesString.get(srcUrl)[k]);
							break;
						case "INTERPOLATION":
							methodUrl = srcUrl;
							break;
						case "IN_TANGENT":
							intangents = new Vec2[sourcesString.get(srcUrl).length / 2];
							int index = 0;
							for (Vec2 v : intangents)
							{
								v.x = Float.parseFloat(sourcesString.get(srcUrl)[index++]);
								v.y = Float.parseFloat(sourcesString.get(srcUrl)[index++]);
							}
							break;
						case "OUT_TANGENT":
							outtangents = new Vec2[sourcesString.get(srcUrl).length / 2];
							index = 0;
							for (Vec2 v : outtangents)
							{
								v.x = Float.parseFloat(sourcesString.get(srcUrl)[index++]);
								v.y = Float.parseFloat(sourcesString.get(srcUrl)[index++]);
							}
							break;
					}
					curAnim = (Element) curAnim.getParentNode();
				}

				assert methodUrl!=null &&
				       input!=null
				       && output!=null;
				for (int j = 0; j < sourcesString.get(methodUrl).length; j++)
				{
					switch (sourcesString.get(methodUrl)[j])
					{
						case "LINEAR":
//							keyframes.add(new LinearKeyframe(new Vec2(input[j], output[j])));
							break;
						case "BEZIER":
//							keyframes.add(new BezierKeyframe(new Vec2(input[j], output[j]), outtangents[j], intangents[j]));
							break;
					}
				}


			}
		}

//		animations.add(new Animation(keyframes));

		return animations;
	}

	private Material getMaterial(Element materialElement,
	                             Map<String, Element> libraryImages,
	                             Map<String, Element> libraryEffects)
	{
		String effectId = ((Element)materialElement.getElementsByTagName("instance_effect").item(0))
				.getAttribute("url").substring(1);
		Element effect = libraryEffects.get(effectId);
		if(effect!=null)
		{
			Material m = new Material(materialElement.getAttribute("id"));
			effect = (Element)((Element)((Element) effect.getElementsByTagName("profile_COMMON").item(0))
					.getElementsByTagName("technique").item(0))
					.getElementsByTagName("phong").item(0); //TODO: suporte para outros modelos de ilum.?

			Element e = (Element) ((Element)effect.getElementsByTagName("diffuse").item(0))
					.getElementsByTagName("color").item(0);
			String[] c;
			if(e != null)
			{
				c = e.getTextContent().split("[ ]");
				m.setAmbient(Float.parseFloat(c[0]), Float.parseFloat(c[1]), Float.parseFloat(c[2]));
			}

			e = (Element) ((Element)effect.getElementsByTagName("diffuse").item(0))
					.getElementsByTagName("color").item(0);
			if(e != null)
			{
				c = e.getTextContent().split("[ ]");
				m.setDiffuse(Float.parseFloat(c[0]), Float.parseFloat(c[1]), Float.parseFloat(c[2]));
			}
			e = (Element) ((Element)effect.getElementsByTagName("diffuse").item(0))
					.getElementsByTagName("texture").item(0);
			if(e != null)
			{
				String texName = e.getAttribute("texture").split("[-]")[0]; // Improvisacao, usar library_images
				c = texName.split("[_]");
				texName = c[0] + "." + c[1];
				m.setTexture(texName);
			}

			e = (Element) ((Element)effect.getElementsByTagName("specular").item(0))
					.getElementsByTagName("color").item(0);
			if(e != null)
			{
				c = e.getTextContent().split("[ ]");
				m.setSpecular(Float.parseFloat(c[0]),Float.parseFloat(c[1]),Float.parseFloat(c[2]));
			}

			e = (Element) ((Element)effect.getElementsByTagName("shininess").item(0))
					.getElementsByTagName("float").item(0);
			if(e != null)
				m.setShininess(Float.parseFloat(e.getTextContent()));

			m.setTransparency(1);

			return m;
		}
		else
			return null;
	}

	@Override
	public String getFilePath()
	{
		return filePath;
	}
}
