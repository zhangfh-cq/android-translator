<?php

class DataBaseManager
{
    private $hostname;
    private $username;
    private $password;
    private $database;
    private $port;
    private $conn;

    public function __construct($hostname, $username, $password, $database, $port)
    {
        $this->hostname = $hostname;
        $this->username = $username;
        $this->password = $password;
        $this->database = $database;
        $this->port = $port;
    }

    public function connect()
    {
        $this->conn = new mysqli($this->hostname, $this->username, $this->password, $this->database, $this->port);
        if ($this->conn->connect_error !== null) {
            throw new Exception($this->conn->connect_error);
        } else {
            return true;
        }
    }

    public function query($sql)
    {
        $result = $this->conn->query($sql);
        if ($result === false) {
            throw new Exception('执行SQL语句：[' . $sql . ']失败');
        } else {
            return $result;
        }
    }

    public function close()
    {
        $this->conn->close();
    }

    public function real_escape_string($string){
        return $this->conn->real_escape_string($string);
    }
}
