package com.strefiz.multicore.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.CustomValue.CvObject;
import net.fabricmc.loader.api.metadata.ModMetadata;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the pure {@link ModClassifier}.
 */
class ModClassifierTest {

	@Test
	void missingMulticoreKeyIsLegacy() {
		ModMetadata metadata = metadataWithCustomValues(Collections.emptyMap());

		assertSame(ModClassification.LEGACY, ModClassifier.classify(metadata));
	}

	@Test
	void nonObjectMulticoreValueIsLegacy() {
		Map<String, CustomValue> values = new java.util.HashMap<>();
		values.put("multicore", customValueString("not-an-object"));
		ModMetadata metadata = metadataWithCustomValues(values);

		assertSame(ModClassification.LEGACY, ModClassifier.classify(metadata));
	}

	@Test
	void multicoreObjectWithoutThreadSafeIsLegacy() {
		Map<String, CustomValue> values = new java.util.HashMap<>();
		values.put("multicore", customValueObject(Collections.emptyMap()));
		ModMetadata metadata = metadataWithCustomValues(values);

		assertSame(ModClassification.LEGACY, ModClassifier.classify(metadata));
	}

	@Test
	void nonBooleanThreadSafeIsLegacy() {
		Map<String, CustomValue> values = new java.util.HashMap<>();
		Map<String, CustomValue> inner = new java.util.HashMap<>();
		inner.put("threadSafe", customValueString("yes"));
		values.put("multicore", customValueObject(inner));
		ModMetadata metadata = metadataWithCustomValues(values);

		assertSame(ModClassification.LEGACY, ModClassifier.classify(metadata));
	}

	@Test
	void threadSafeFalseIsLegacy() {
		Map<String, CustomValue> values = new java.util.HashMap<>();
		Map<String, CustomValue> inner = new java.util.HashMap<>();
		inner.put("threadSafe", customValueBoolean(false));
		values.put("multicore", customValueObject(inner));
		ModMetadata metadata = metadataWithCustomValues(values);

		assertSame(ModClassification.LEGACY, ModClassifier.classify(metadata));
	}

	@Test
	void threadSafeTrueIsCompatible() {
		Map<String, CustomValue> values = new java.util.HashMap<>();
		Map<String, CustomValue> inner = new java.util.HashMap<>();
		inner.put("threadSafe", customValueBoolean(true));
		values.put("multicore", customValueObject(inner));
		ModMetadata metadata = metadataWithCustomValues(values);

		assertSame(ModClassification.COMPATIBLE, ModClassifier.classify(metadata));
	}

	@Test
	void nullMetadataIsLegacy() {
		assertSame(ModClassification.LEGACY, ModClassifier.classify(null));
	}

	// -------- test doubles --------

	private static ModMetadata metadataWithCustomValues(Map<String, CustomValue> values) {
		return new ModMetadata() {
			@Override
			public boolean containsCustomValue(String key) {
				return values.containsKey(key);
			}

			@Override
			public CustomValue getCustomValue(String key) {
				return values.get(key);
			}

			@Override
			public Map<String, CustomValue> getCustomValues() {
				return values;
			}

			@Override
			public String getId() {
				return "test-mod";
			}

			@Override
			public String getType() {
				return "jar";
			}

			@Override
			public String getName() {
				return "Test Mod";
			}

			@Override
			public String getDescription() {
				return "";
			}

			@Override
			public net.fabricmc.loader.api.metadata.ModEnvironment getEnvironment() {
				return net.fabricmc.loader.api.metadata.ModEnvironment.SERVER;
			}

			@Override
			public net.fabricmc.loader.api.Version getVersion() {
				return new net.fabricmc.loader.api.Version() {
					@Override
					public int compareTo(net.fabricmc.loader.api.Version o) {
						return 0;
					}

					@Override
					public String getFriendlyString() {
						return "1.0.0";
					}

					@Override
					public String toString() {
						return "1.0.0";
					}
				};
			}

			@Override
			public Collection<String> getProvides() {
				return Collections.emptyList();
			}

			@Override
			public Collection<net.fabricmc.loader.api.metadata.ModDependency> getDependencies() {
				return Collections.emptyList();
			}

			@Override
			public Collection<net.fabricmc.loader.api.metadata.ModDependency> getDepends() {
				return Collections.emptyList();
			}

			@Override
			public Collection<net.fabricmc.loader.api.metadata.ModDependency> getRecommends() {
				return Collections.emptyList();
			}

			@Override
			public Collection<net.fabricmc.loader.api.metadata.ModDependency> getSuggests() {
				return Collections.emptyList();
			}

			@Override
			public Collection<net.fabricmc.loader.api.metadata.ModDependency> getConflicts() {
				return Collections.emptyList();
			}

			@Override
			public Collection<net.fabricmc.loader.api.metadata.ModDependency> getBreaks() {
				return Collections.emptyList();
			}

			@Override
			public Collection<net.fabricmc.loader.api.metadata.Person> getAuthors() {
				return Collections.emptyList();
			}

			@Override
			public Collection<net.fabricmc.loader.api.metadata.Person> getContributors() {
				return Collections.emptyList();
			}

			@Override
			public net.fabricmc.loader.api.metadata.ContactInformation getContact() {
				return null;
			}

			@Override
			public Collection<String> getLicense() {
				return Collections.emptyList();
			}

			@Override
			public java.util.Optional<String> getIconPath(int size) {
				return java.util.Optional.empty();
			}

			@Override
			public boolean containsCustomElement(String key) {
				return values.containsKey(key);
			}
		};
	}

	private static CustomValue customValueString(String value) {
		return new CustomValue() {
			@Override
			public CvType getType() {
				return CvType.STRING;
			}

			@Override
			public CvObject getAsObject() {
				return null;
			}

			@Override
			public CustomValue.CvArray getAsArray() {
				return null;
			}

			@Override
			public String getAsString() {
				return value;
			}

			@Override
			public Number getAsNumber() {
				return null;
			}

			@Override
			public boolean getAsBoolean() {
				return false;
			}
		};
	}

	private static CustomValue customValueObject(Map<String, CustomValue> entries) {
		return new CustomValue() {
			@Override
			public CvType getType() {
				return CvType.OBJECT;
			}

			@Override
			public CvObject getAsObject() {
				return new CvObject() {
					@Override
					public int size() {
						return entries.size();
					}

					@Override
					public boolean containsKey(String key) {
						return entries.containsKey(key);
					}

					@Override
					public CustomValue get(String key) {
						return entries.get(key);
					}

					@Override
					public java.util.Iterator<Map.Entry<String, CustomValue>> iterator() {
						return entries.entrySet().iterator();
					}

					@Override
					public CvType getType() {
						return CvType.OBJECT;
					}

					@Override
					public CvObject getAsObject() {
						return null;
					}

					@Override
					public CustomValue.CvArray getAsArray() {
						return null;
					}

					@Override
					public String getAsString() {
						return null;
					}

					@Override
					public Number getAsNumber() {
						return null;
					}

					@Override
					public boolean getAsBoolean() {
						return false;
					}
				};
			}

			@Override
			public CustomValue.CvArray getAsArray() {
				return null;
			}

			@Override
			public String getAsString() {
				return null;
			}

			@Override
			public Number getAsNumber() {
				return null;
			}

			@Override
			public boolean getAsBoolean() {
				return false;
			}
		};
	}

	private static CustomValue customValueBoolean(boolean value) {
		return new CustomValue() {
			@Override
			public CvType getType() {
				return CvType.BOOLEAN;
			}

			@Override
			public CvObject getAsObject() {
				return null;
			}

			@Override
			public CustomValue.CvArray getAsArray() {
				return null;
			}

			@Override
			public String getAsString() {
				return null;
			}

			@Override
			public Number getAsNumber() {
				return null;
			}

			@Override
			public boolean getAsBoolean() {
				return value;
			}
		};
	}
}
