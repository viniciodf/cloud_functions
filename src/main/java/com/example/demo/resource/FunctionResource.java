package com.example.demo.resource;

import com.example.demo.model.Function;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.Credentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.functions.v1.*;
import com.google.iam.v1.Binding;
import com.google.iam.v1.SetIamPolicyRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileInputStream;
import java.io.IOException;

@RestController
public class FunctionResource {

    @PostMapping("/functions")
    String newFunction(@RequestBody Function functionRequest) throws Exception {
        try {
            Credentials myCredentials = ServiceAccountCredentials.fromStream(new FileInputStream("/Users/vinicio/dev/auth.json"));
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
