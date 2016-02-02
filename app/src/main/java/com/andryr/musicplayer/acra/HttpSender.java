/*
 * Copyright 2016 andryr
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.andryr.musicplayer.acra;

import android.content.Context;

import org.acra.ACRA;
import org.acra.ACRAConfiguration;
import org.acra.collector.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;
import org.acra.util.JSONReportBuilder.JSONReportException;

import java.net.MalformedURLException;
import java.net.URL;


public class HttpSender implements ReportSender {
    private String formUri;
    private String login;
    private String password;
    public HttpSender() {
        formUri = ACRA.getConfig().formUri();
        login = ACRAConfiguration.isNull(ACRA.getConfig().formUriBasicAuthLogin()) ? null : ACRA
                .getConfig().formUriBasicAuthLogin();
        password = ACRAConfiguration.isNull(ACRA.getConfig().formUriBasicAuthPassword()) ? null : ACRA
                .getConfig().formUriBasicAuthPassword();
    }


    @Override
    public void send(Context context, CrashReportData report) throws ReportSenderException {

        try {




            final String reportAsString = report.toJSON().toString();

            HttpSenderService.sendReport(context, formUri, reportAsString, login, password);


        } catch (JSONReportException e) {
            throw new ReportSenderException("Error while sending " + ACRA.getConfig().reportType()
                    + " report via Http " + ACRA.getConfig().httpMethod().name(), e);
        }
    }


}
