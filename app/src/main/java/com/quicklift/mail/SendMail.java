/**
 * Copyright (c) 2017 JMD Techmosoft Pvt Ltd - All rights reserved.
 * This software is authored by JMD Techmosoft Pvt Ltd and is its's intellectual
 * property, including the copyrights in all countries in the world.
 * This software may be provided under a license to use only while all other rights,
 * including ownership rights, being retained by JMD Techmosoft Pvt Ltd.
 * This file can not be distributed, copied, or reproduced in any manner,
 * electronic or otherwise, without the written consent of JMD Techmosoft Pvt Ltd.
 *
 */

package com.quicklift.mail;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class SendMail extends AsyncTask<Void,Void,Void> {

    //Declaring Variables
    private Context context;
    private Session session;
    //Information to send email
    private String email;
    private String subject;
    private String message;
    //Progressdialog to show while sending email
    private ProgressDialog progressDialog;
    private String path="";
    private String id="",password="";

    //Class Constructor
    public SendMail(Context context, String email, String subject, String message, String id, String password){
        //Initializing variables
        this.context = context;
        this.email = email;
        this.subject = subject;
        this.message = message;
        this.id = id;
        this.password = password;
        path= Environment.getExternalStorageDirectory().getPath() + "/invoice.pdf";
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        progressDialog=new ProgressDialog(context,ProgressDialog.THEME_HOLO_DARK);
        progressDialog.setMessage("Sending Invoice ...");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.show();
        //Showing progress dialog while sending email
//        progressDialog = ProgressDialog.show(context,"Sending Verification Mail","Please wait...",false,false);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        //Dismissing the progress dialog
  //      progressDialog.dismiss();
        //Showing a success message
        //Toast.makeText(context,"Mail Sent",Toast.LENGTH_LONG).show();
        progressDialog.dismiss();
        File file=new File(path);
        if (file.exists())
            file.delete();
        Toast.makeText(context, "Invoice sent to your email id !", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected Void doInBackground(Void... params) {
        //Creating properties
        Properties props = new Properties();

        //Configuring properties for gmail
        //If you are not using gmail you may need to change the values
        props.put("mail.smtp.host", "mail.quicklift.in");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "26");

        //Creating a new session
        session = Session.getDefaultInstance(props,
                new javax.mail.Authenticator() {
                    //Authenticating the password
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(id, password);
                    }
                });

        try {
            //Creating MimeMessage object
            MimeMessage mm = new MimeMessage(session);

            //Setting sender address
            mm.setFrom(new InternetAddress(id));
            //Adding receiver
            mm.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
            //Adding subject
            mm.setSubject(subject);
            //Adding message
            mm.setText(message);

            if(!path.equals("")) {
                MimeBodyPart messageBodyPart = new MimeBodyPart();
                // fill message
                messageBodyPart.setText(message);

                Multipart multipart = new MimeMultipart();
                multipart.addBodyPart(messageBodyPart);

                messageBodyPart = new MimeBodyPart();
                DataSource source = new FileDataSource(path);
                messageBodyPart.setDataHandler(new DataHandler(source));
                messageBodyPart.setFileName("Invoice.pdf");
                multipart.addBodyPart(messageBodyPart);


                mm.setContent(multipart);
            }
            //Sending email
           Transport.send(mm);

        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return null;
    }
}