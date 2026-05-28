package com.zalphion.featurecontrol.bundle;

import argo.InvalidSyntaxException;
import argo.JsonParser;
import argo.jdom.JsonField;
import argo.jdom.JsonNode;
import com.zalphion.featurecontrol.lib.result.Failure;
import com.zalphion.featurecontrol.lib.result.Result;
import com.zalphion.featurecontrol.lib.result.Success;
import org.jspecify.annotations.NonNull;
import java.util.stream.Collectors;
import lombok.val;

import static argo.jdom.JsonNodeSelectors.*;

public class FeatureBundleSerializer {

    private static final String
            PROPERTIES = "properties",
            FLAGS = "flags",
            OVERRIDES = "overrides",
            BUCKETS = "buckets",
            SALT_HEX = "saltHex",
            BUCKET_NAME = "name",
            BUCKET_THRESHOLD = "threshold";

    public static @NonNull Result<FeatureBundle> fromJson(@NonNull @lombok.NonNull String json) {
        try {
            val bundle = new JsonParser().parse(json);
            return new Success<>(fromJson(bundle));
        } catch (InvalidSyntaxException e) {
            return new Failure<>("Error parsing feature bundle JSON: " + e.getMessage());
        }
    }

    // TODO support streaming
    private static @NonNull FeatureBundle fromJson(@NonNull @lombok.NonNull JsonNode bundle) {
        val properties = anObjectNodeWithField(PROPERTIES).getValue(bundle)
                .getFieldList().stream().collect(Collectors.toMap(
                        JsonField::getNameText,
                        property -> aStringNode().getValue(property.getValue())
                ));

        val flags = anObjectNodeWithField(FLAGS).getValue(bundle)
                .getFieldList().stream().collect(Collectors.toMap(
                        JsonField::getNameText,
                flag -> parseFlag(flag.getValue())
                ));

        return new FeatureBundle(properties, flags);
    }

    private static @NonNull FlagBundle parseFlag(@NonNull JsonNode node) {
        return new FlagBundle(
                anObjectNodeWithField(OVERRIDES).getValue(node)
                        .getFieldList().stream().collect(Collectors.toMap(
                                JsonField::getNameText,
                        override -> aStringNode().getValue(override.getValue())
                        )),
                anArrayNode(BUCKETS).getValue(node).stream().map(bucket ->
                        new VariantBucket(
                                aStringNode(BUCKET_NAME).getValue(bucket),
                                Integer.parseInt(aNumberNode(BUCKET_THRESHOLD).getValue(bucket))
                        )
                ).collect(Collectors.toList()),
                aStringNode(SALT_HEX).getValue(node)
        );
    }
}
