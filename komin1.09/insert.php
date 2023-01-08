<?php 

    error_reporting(E_ALL); 
    ini_set('display_errors',1); 

    include('dbcon.php');


    $android = strpos($_SERVER['HTTP_USER_AGENT'], "Android");


    if( (($_SERVER['REQUEST_METHOD'] == 'POST') && isset($_POST['submit'])) || $android )
    {

        // 안드로이드 코드의 postParameters 변수에 적어준 이름을 가지고 값을 전달 받습니다.

        $name=$_POST['name'];
        $age=$_POST['age'];
		$gender=$_POST['gender'];
		$latitude=$_POST['latitude'];
		$longitude=$_POST['longitude'];

        if(empty($name)){
            $errMSG = "이름을 입력하세요.";
        }
        else if(empty($age)){
            $errMSG = "나이를 입력하세요.";
        }
		
		else if(empty($gender)){
            $errMSG = "성별을 입력하세요.";
		}
		
		else if(empty($latitude)){
            $errMSG = "위도를 입력하세요.";
        }
		
		else if(empty($longitude)){
            $errMSG = "경도를 입력하세요.";
        }

        if(!isset($errMSG)) // 이름과 나라 모두 입력이 되었다면 
        {
            try{
                // SQL문을 실행하여 데이터를 MySQL 서버의 person 테이블에 저장합니다. 
                $stmt = $con->prepare('INSERT INTO kid(name, age, gender, latitude, longitude) VALUES(:name, :age, :gender, :latitude, :longitude)');
                $stmt->bindParam(':name', $name);
                $stmt->bindParam(':age', $age);
				$stmt->bindParam(':gender', $gender);
				$stmt->bindParam(':latitude', $latitude);
				$stmt->bindParam(':longitude', $longitude );
				

                if($stmt->execute())
                {
                    $successMSG = "새로운 사용자를 추가했습니다.";
                }
                else
                {
                    $errMSG = "사용자 추가 에러";
                }

            } catch(PDOException $e) {
                die("Database error: " . $e->getMessage()); 
            }
        }

    }

?>


<?php 
    if (isset($errMSG)) echo $errMSG;
    if (isset($successMSG)) echo $successMSG;

	$android = strpos($_SERVER['HTTP_USER_AGENT'], "Android");
   
    if( !$android )
    {
?>
    <html>
       <body>

            <form action="<?php $_PHP_SELF ?>" method="POST">
                Name: <input type = "text" name = "name" />
				Age: <input type = "text" age = "age" />
				Gender: <input type = "text" gender = "gender" />
				Latitude: <input type = "text" latitude = "latitude" />
                Longitude: <input type = "text" longitude = "longitude" />
                <input type = "submit" name = "submit" />
            </form>
       
       </body>
    </html>

<?php 
    }
?>