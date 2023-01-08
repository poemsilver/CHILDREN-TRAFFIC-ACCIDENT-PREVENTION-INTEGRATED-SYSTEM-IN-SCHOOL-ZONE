<?php

    $host = 'localhost';
    $username = ''; # MySQL 계정 아이디
    $password = ''; # MySQL 계정 패스워드
    $dbname = 'komindb';  # DATABASE 이름


    $options = array(PDO::MYSQL_ATTR_INIT_COMMAND => 'SET NAMES utf8');
    
    try {

        $con = new PDO("mysql:host={$host};dbname={$dbname};charset=utf8",$username, $password);
    } catch(PDOException $e) {

        die("Failed to connect to the database: " . $e->getMessage()); 
    }	


    $con->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    $con->setAttribute(PDO::ATTR_DEFAULT_FETCH_MODE, PDO::FETCH_ASSOC);

 
    header('Content-Type: text/html; charset=utf-8'); 
    #session_start();
?>