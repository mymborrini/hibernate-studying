
    alter table MESSAGES 
        drop constraint FK_NEXT_MESSAGE;

    drop table MESSAGES if exists;

    create table MESSAGES (
        MESSAGE_ID bigint not null,
        MESSAGE_TEXT varchar(255),
        NEXT_MESSAGE_ID bigint,
        primary key (MESSAGE_ID)
    );

    alter table MESSAGES 
        add constraint FK_NEXT_MESSAGE 
        foreign key (NEXT_MESSAGE_ID) 
        references MESSAGES;
