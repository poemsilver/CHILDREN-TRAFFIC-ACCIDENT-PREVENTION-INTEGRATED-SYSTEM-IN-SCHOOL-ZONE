<?php 

    error_reporting(E_ALL); 
    ini_set('display_errors',1); 

    include('dbcon.php');
        

    $stmt = $con->prepare('select * from kid where name = name order by id desc limit 1');
    $stmt->execute();

    if ($stmt->rowCount() > 0)
    {
        $data = array(); 

        while($row=$stmt->fetch(PDO::FETCH_ASSOC))
        {
            extract($row);
    
            array_push($data, 
                array('id'=>$id,
                'name'=>$name,
                'age'=>$age,
				'gender'=>$gender,
				'latitude'=>$latitude,
				'longitude'=>$longitude
            ));
        }

        header('Content-Type: application/json; charset=utf8');
        $json = json_encode(array("komin-e"=>$data), JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);
        echo $json;
    }

?>