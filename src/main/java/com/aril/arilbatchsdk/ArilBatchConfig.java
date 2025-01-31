package com.aril.arilbatchsdk;

import com.aril.arilminiosdk.annotation.EnableArilMinio;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableArilMinio
@EnableAutoConfiguration
@ComponentScan("com.aril.arilbatchsdk")
public class ArilBatchConfig {
}
