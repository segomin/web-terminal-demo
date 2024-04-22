package org.aalku.demo.term;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.dsl.ExecListener;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class K8sClientProvider {


    public static ExecWatch k8sExecWatch(String namespace, String podName) {
        String[] command = { "/bin/sh" };

        log.info("Opening k8s exec connection in : " + podName);

        var client = kubernetesClient();
        var execWatch = client.pods().inNamespace(namespace).withName(podName)
                .redirectingInput() //or .readingInput(System.in or ...)
                .redirectingOutput() //or .writingOutput(System.out or ...)
                .redirectingError() //or .writingError(System.err or ...)
                .withTTY()
                .usingListener(new SimpleListener())
                .exec(command);
        return execWatch;
    }


    public static KubernetesClient kubernetesClient() {
        // todo read from env or something
        Config config = new ConfigBuilder()
                .withMasterUrl("https://k8s-host.com:6443")
                .withTrustCerts(true)
                .withCaCertData("...")
                .withClientCertData("...")
                .withClientKeyData("...")
                .withClientKeyAlgo("EC") // need: org.bouncycastle:bcpkix-jdk15on:1.68
                .build();
        return new KubernetesClientBuilder().withConfig(config).build();
    }


    private static class SimpleListener implements ExecListener {
        @Override
        public void onOpen() {
            log.info("Shell was opened");
        }

        @Override
        public void onFailure(Throwable t, Response response) {
            log.info("Shell barfed");
        }

        @Override
        public void onClose(int code, String reason) {
            log.info("Shell closed");
        }

    }
}
