package com.commercetools.emailprocessor.model;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.activation.DataSource;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.util.JSONPObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.util.MimeMessageParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Email implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(Email.class);
    public static final String PENDING = "pending";
    public static final String ERROR = "error";

    private String status = "";
    private String lastError = "";

    private List<InternetAddress> to = null;
    private List<InternetAddress> cc = null;
    private List<InternetAddress> bcc = null;
    private InternetAddress from = null;
    private String subject = "";
    private JsonNode body = null;
    List<String> attachments = new ArrayList();

    public Email() {
    }

    public Email(HtmlEmail email, String status, String errorMessage) {

        this.status = status;
        this.lastError = lastError;

        to = email.getToAddresses();
        from = email.getFromAddress();
        cc = email.getCcAddresses();
        bcc = email.getBccAddresses();

        subject = email.getSubject();
        fetchBodyAndAttachments(email.getMimeMessage());
    }



    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public List<InternetAddress> getTo() {
        return to;
    }

    public void setTo(List<InternetAddress> to) {
        this.to = to;
    }

    public List<InternetAddress> getCc() {
        return cc;
    }

    public void setCc(List<InternetAddress> cc) {
        this.cc = cc;
    }

    public List<InternetAddress> getBcc() {
        return bcc;
    }

    public void setBcc(List<InternetAddress> bcc) {
        this.bcc = bcc;
    }

    public InternetAddress getFrom() {
        return from;
    }

    public void setFrom(InternetAddress from) {
        this.from = from;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public List<String> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<String> attachments) {
        this.attachments = attachments;
    }

    private void fetchBodyAndAttachments(Object msgContent) {
        try {
            String content = "";
            if (msgContent instanceof MimeMessage) {
                MimeMessageParser parser = new MimeMessageParser((MimeMessage) msgContent);
                parser.parse();
                // fetch body
                content = parser.getHtmlContent();
                JsonNode jsonNode = TextNode.valueOf(content);
                 body = jsonNode;
// fetch attachments
                List<DataSource> attachmentList = parser.getAttachmentList();
                for (DataSource dataSource : attachmentList) {
                    attachments.add(dataSource.getName());
                }
            } else {
                content = msgContent.toString();
            }
        } catch (Exception e) {
            LOG.error("Cannot parse email body");
        }
    }

    public HtmlEmail toHtmlMail() {
        HtmlEmail email = new HtmlEmail();
        email.setCharset("utf-8");
        try {
            if (from != null) {

                email.setFrom(from.getAddress());

            }
            if (to != null) {
                for (InternetAddress toAddress : to) {
                    email.addTo(toAddress.getAddress());
                }
            }
            if (cc != null) {
                for (InternetAddress ccAddress : cc) {
                    email.addTo(ccAddress.getAddress());
                }
            }
            if (bcc != null) {
                for (InternetAddress bccAddress : bcc) {
                    email.addTo(bccAddress.getAddress());
                }
            }
            if (StringUtils.isNotEmpty(subject)) {
                email.setSubject(subject);
            }
            if (body != null) {
                String mailContent = body.textValue()!=null ? body.textValue(): "";
                email.setHtmlMsg(mailContent);
            }
        } catch (EmailException e) {
            LOG.error("Cannot create htmlEmail", e);
        }
        return email;
    }

    public JsonNode getBody() {
        return body;
    }

    public void setBody( JsonNode body) {
        this.body = body;
    }
}
