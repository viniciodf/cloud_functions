package com.example.demo.resource;

import com.example.demo.model.Function;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.Credentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.MonitoredResource;
import com.google.cloud.functions.v1.*;
import com.google.cloud.logging.*;
import com.google.common.collect.ImmutableMap;
import com.google.iam.v1.Binding;
import com.google.iam.v1.SetIamPolicyRequest;
import com.google.protobuf.Any;
import com.google.protobuf.StringValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@RestController
public class FunctionResource {

    @PostMapping("/functions")
    String newFunction(@RequestBody Function functionRequest) throws Exception {
        Credentials myCredentials = ServiceAccountCredentials.fromStream(new FileInputStream("/Users/vinicio/dev/auth.json"));
        try {
            String location = LocationName.of(functionRequest.getProjectId(), "us-central1").toString();

            CloudFunctionsServiceSettings settings = CloudFunctionsServiceSettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(myCredentials)).build();

            CloudFunctionsServiceClient cloudFunctionsServiceClient = CloudFunctionsServiceClient.create(settings);

            Binding binding = Binding.newBuilder().addMembers("allUsers").setRole("roles/cloudfunctions.invoker").build();
            SetIamPolicyRequest policy = SetIamPolicyRequest.newBuilder().setResource(CloudFunctionName.of(functionRequest.getProjectId(), "us-central1", functionRequest.getName()).toString()).setPolicy(com.google.iam.v1.Policy.newBuilder().addBindings(binding)).build();

            CloudFunction function = CloudFunction.newBuilder()
                    .setName(location + "/functions/" + functionRequest.getName())
                    .setEntryPoint("helloHttp")
                    .setHttpsTrigger(HttpsTrigger.getDefaultInstance())
                    .setRuntime("nodejs16")
                    .setSourceArchiveUrl("gs://gcf-sources-377676152709-us-central1/helloHttp-96504daf-7801-48b0-a0c9-8d20e4296a10_version-1_function-source.zip")
                    .build();

            CloudFunction response = cloudFunctionsServiceClient.createFunctionAsync(location, function).get();
            cloudFunctionsServiceClient.setIamPolicy(policy);

            return response.getName() + " - " + response.getHttpsTrigger().getUrl();
        } catch (Exception e) {

            Logging logging = LoggingOptions.newBuilder().setCredentials(myCredentials).build().getService();
            Map<String, String> payload =
                    ImmutableMap.of(
                            "function", functionRequest.getName(), "projectCustom", functionRequest.getProjectId(), "paramTest", "alou");

            Payload.JsonPayload payloadAudit = Payload.JsonPayload.of(payload);

            LogEntry entry =
                    LogEntry.newBuilder(payloadAudit)
                            .setSeverity(Severity.INFO)
                            .setLogName("testLog")
                            .setResource(MonitoredResource.newBuilder("global").build())
                            .build();

            logging.write(Collections.singleton(entry));


            e.printStackTrace();
            throw e;
        }
    }

    @PutMapping("/functions")
    String executeFunction(@RequestBody Function functionRequest) throws IOException {
        try {
            Credentials myCredentials = ServiceAccountCredentials.fromStream(new FileInputStream("/Users/vinicio/dev/auth.json"));
            CloudFunctionsServiceSettings settings = CloudFunctionsServiceSettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(myCredentials)).build();

            CloudFunctionsServiceClient cloudFunctionsServiceClient = CloudFunctionsServiceClient.create(settings);
            String location = LocationName.of(functionRequest.getProjectId(), "us-central1").toString();
            String name = location + "/functions/" + functionRequest.getName();
            CallFunctionResponse response = cloudFunctionsServiceClient.callFunction(CallFunctionRequest.newBuilder().setName(name).build());
            return response.getResult();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
