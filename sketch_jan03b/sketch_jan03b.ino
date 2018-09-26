const int ELECTRO_VANNE       = 3;
const int ALLUMEUR            = 11;
const int ELECTRO_MODULATEUR  = 8;
const int EXTRACTEUR          = 9;
const int IN_ASPI_BAS         = 4;
const int IN_ASPI_HAUT        = 5;

// Etats de la chaudiere
const int CHAUDIERE_OFF_NORMAL        = 0;
const int CHAUDIERE_ALLUMAGE          = 1;
const int CHAUDIERE_ON_CHAUFFE        = 2;
const int CHAUDIERE_EXTINCTION        = 3;
const int CHAUDIERE_REFROIDISSEMENT   = 4;
const int CHAUDIERE_PAUSE             = 5;
const int CHAUDIERE_ON_ERREUR         = 10;
const int CHAUDIERE_OFF_ERREUR        = 11;
const int CHAUDIERE_ARRET_ERREUR      = 12;

int Etat_Chaudiere = CHAUDIERE_OFF_NORMAL;
bool Etat_Extracteur = false ; // false = OFF, true = ON
// Consigne
int consigne = 18;

// VALEURS des Capteurs
int Val_Capt_Flamme               = 0;
int Val_Capt_Flamme_Gauche        = 0;
int Val_Capt_Temperature_Eau      = 0;
int Val_Capt_Temperature_Eau_Bas  = 0;
int Val_Capt_Aspi_Bas             = 0;
int Val_Capt_Aspi_Haut            = 0;

// Variables Temps
unsigned long Temps_Init        = 0;
unsigned long Temps_Allumage    = 0;
unsigned long Temps_Chauffe     = 0;
unsigned long Temps_Extinction  = 0;
unsigned long Temps_Refroidis   = 0;

// Tentatives d'allumages
int nb_essais_allumage = 0;

// Variable de relance de l'electromodulateur
int Relance_Relais_Electromodulateur = 0;

// Variable de la temperature juste avant chauffe (sert pour detecter une absence de chauffe => pb possible de circulateur)
int Temperature_Init = 0;


void AfficherEtat(String msg) {
    Serial.println(msg);
    Serial.print(F("    ETAT CHAUDIERE = "));   Serial.println(Etat_Chaudiere);
    Serial.print(F("    ETAT EXTRACTEUR = "));   Serial.println(Etat_Extracteur);
    Serial.print(F("    Capteur Flamme Droit = ")); Serial.println(Val_Capt_Flamme);
    Serial.print(F("    Capteur Flamme Gauche = ")); Serial.println(Val_Capt_Flamme_Gauche);
    Serial.print(F("    T° Eau Haut = ")); Serial.println(Val_Capt_Temperature_Eau);
    Serial.print(F("    T° Eau Bas = ")); Serial.println(Val_Capt_Temperature_Eau_Bas);
    Serial.print(F("    T° Initiale = ")); Serial.println(Temperature_Init);
    Serial.print(F("    Consigne = ")); Serial.println(consigne);
    Serial.print(F("    Aspi Haut  = ")); Serial.println(Val_Capt_Aspi_Haut);
    Serial.print(F("    Aspi Bas   = ")); Serial.println(Val_Capt_Aspi_Bas);
    Serial.print(F("    millis         = ")); Serial.println(millis());
    Serial.print(F("    Tps_Init       = ")); Serial.println(Temps_Init);
    Serial.print(F("    Tps_Allumage   = ")); Serial.println(Temps_Allumage);
    Serial.print(F("    Tps_Chauffe    = ")); Serial.println(Temps_Chauffe);
    Serial.print(F("    Tps_Extinction = ")); Serial.println(Temps_Extinction);
    Serial.print(F("    Tps_Refroidis  = ")); Serial.println(Temps_Refroidis);
    
}

void ArreterActionneurs() {
    // Coupe ElectroVanne + ElectroRegulateur
    digitalWrite(ELECTRO_VANNE,       HIGH) ;  // Fermeture Electrovanne GAZ
    digitalWrite(ALLUMEUR,            HIGH); // FIN ALLUMEUR
    digitalWrite(ELECTRO_MODULATEUR,  LOW);  // FIN Relance Electromodulateur GAZ
    delay(5000);
    digitalWrite(EXTRACTEUR,          HIGH); // STOP EXTRACTEUR
    // Coupe Circulateur
    delay(2000);
    Etat_Extracteur = false;
    LireCapteurs();
}
void LireCapteurs() {
      // Lecture des capteurs
    Val_Capt_Flamme = analogRead(A0);
    Val_Capt_Flamme_Gauche   = analogRead(A2);
    //int flotte = analogRead(A1);
    Val_Capt_Temperature_Eau     = (int) (135.0f - 1.1f*analogRead(A1));
    Val_Capt_Temperature_Eau_Bas = (int) (135.0f - 1.1f*analogRead(A3));
    //for(int j=0; j<100;j++) {
    Val_Capt_Aspi_Bas  = digitalRead(IN_ASPI_BAS);
    Val_Capt_Aspi_Haut = digitalRead(IN_ASPI_HAUT);
}
//int Thermistor(int RawADC) {
// return 135-RawADC;
//}

void setup() {
  
  // Attendre le boot du Raspberry
  //delay(25*1000); // 25 sec
  // Affectation des Output
  pinMode(ELECTRO_VANNE,            OUTPUT);
  pinMode(ALLUMEUR,                 OUTPUT);
  pinMode(ELECTRO_MODULATEUR,       OUTPUT);
  pinMode(EXTRACTEUR,               OUTPUT);
  pinMode(IN_ASPI_BAS,              INPUT);
  pinMode(IN_ASPI_HAUT,             INPUT);
  
  // Initialisation des Output
  digitalWrite(ELECTRO_VANNE,       HIGH);
  digitalWrite(ALLUMEUR,            HIGH);
  digitalWrite(ELECTRO_MODULATEUR,  LOW);
  digitalWrite(EXTRACTEUR,          HIGH);

  // Communication avec le PC
  Serial.begin(9600);
  // Etat au lancement
  Val_Capt_Flamme   = analogRead(A0);
  Val_Capt_Flamme_Gauche   = analogRead(A2);
  Val_Capt_Temperature_Eau      = (int) (135.0f - 1.1f*analogRead(A1));
  Val_Capt_Temperature_Eau_Bas  = (int) (135.0f - 1.1f*analogRead(A3));
  Etat_Chaudiere    = CHAUDIERE_OFF_NORMAL;
  Val_Capt_Aspi_Bas  = digitalRead(IN_ASPI_BAS);
  Val_Capt_Aspi_Haut = digitalRead(IN_ASPI_HAUT);
  
  // DEBUG
  //Etat_Chaudiere = CHAUDIERE_ON_CHAUFFE;  Temps_Chauffe = millis() - 240000;
  AfficherEtat("Lancement");
  
  // Debloque allumeur
  /*for(int allum=0;allum < 30; allum++) {
    digitalWrite(ALLUMEUR,       LOW); // Lancement relais ALLUMEUR
    delay(110); // Etincelles
    digitalWrite(ALLUMEUR,       HIGH); // STOP relais ALLUMEUR
    delay(110); // Repos allumeur
  }*/
    digitalWrite(ALLUMEUR,       LOW); // Lancement relais ALLUMEUR
    delay(1000); // Etincelles
    digitalWrite(ALLUMEUR,       HIGH); // STOP relais ALLUMEUR
    //delay(150-allum+10); // Repos allumeur
  
  
  
  delay(30000); // 30 secondes pour que le Rapbery boot

  // temps au lancement
  Temps_Init = millis();

}

void loop() {

  if (Serial.available() > 0) { 
    byte incomingByte = Serial.read();
    //Serial.print(" I received:"); 
    //Serial.println(incomingByte);
    consigne = (int) incomingByte;
    if(consigne > 25) consigne = 25;
    if(consigne == 0)   Etat_Chaudiere = CHAUDIERE_ON_ERREUR;
    if(consigne == 1) {
      ArreterActionneurs();
      Temps_Init = millis();
      Etat_Chaudiere = CHAUDIERE_OFF_NORMAL;
      consigne = 22;
    }
    if(consigne == 15 && Etat_Chaudiere != CHAUDIERE_PAUSE) {
      ArreterActionneurs();
      Etat_Chaudiere = CHAUDIERE_PAUSE;
    }
    if(consigne > 15 && Etat_Chaudiere == CHAUDIERE_PAUSE ) {
      ArreterActionneurs();
      Temps_Init = millis();
      Etat_Chaudiere = CHAUDIERE_OFF_NORMAL;
    }

  }

    //do {

      // Lecture des capteurs
      LireCapteurs();
    //Val_Capt_Flamme = analogRead(A0);
    //Val_Capt_Flamme_Gauche   = analogRead(A2);
    //int flotte = analogRead(A1);
    //Val_Capt_Temperature_Eau     = (int) (135.0f - 1.1f*analogRead(A1));
    //Val_Capt_Temperature_Eau_Bas = (int) (135.0f - 1.1f*analogRead(A3));
    //for(int j=0; j<100;j++) {
    //Val_Capt_Aspi_Bas  = digitalRead(IN_ASPI_BAS);
    //Val_Capt_Aspi_Haut = digitalRead(IN_ASPI_HAUT);
    //}
    //Val_Capt_Aspi = Val_Capt_Aspi / 100;
    //Serial.print(Val_Capt_Aspi_Bas);
    //Serial.print(", ");
    //Serial.println(Val_Capt_Aspi_Haut);
    //Serial.print(Val_Capt_Temperature_Eau_Bas);
    //Serial.print(", ");
    //Serial.print(Val_Capt_Temperature_Eau);
    //Serial.println(flotte);
    //Serial.print(Val_Capt_Flamme_Gauche );
    //Serial.print(", ");
    //Serial.println(Val_Capt_Flamme);
    //Serial.println(analogRead(A1));
    
    
      // Test de l'aspiration de l'air (CO)
      if( Etat_Chaudiere < 10 ) { // Etats < 10 PAS ENCORE EN ERREUR : le devient si l'aspi n'est pas presente
        if(Etat_Extracteur == true && Val_Capt_Aspi_Bas == 0 && Val_Capt_Aspi_Haut == 1 ) {
          AfficherEtat("!!!!! PAS d'aspiration !!!!!");
          Etat_Chaudiere = CHAUDIERE_ON_ERREUR;
        }
        if(Etat_Extracteur == false && Val_Capt_Aspi_Bas == 1 && Val_Capt_Aspi_Haut == 0 ) {
          AfficherEtat("!!!!! Capteur Aspiration ON alors qu'il devrait etre a OFF (raisons: capteur bloqué, moteur d'extraction en route alors qu'il devrait etre eteint) !!!!!");
          Etat_Chaudiere = CHAUDIERE_ON_ERREUR;
        }
        if( Val_Capt_Aspi_Bas == Val_Capt_Aspi_Haut ) {
          AfficherEtat("!!!!! CAPTEUR d'aspiration PROBLEME !!!!!");
          Etat_Chaudiere = CHAUDIERE_ON_ERREUR;
        }
      
      }

      // LANCEMENT EXTRACTION (2 sec avant ALLUMAGE)
      if( Etat_Extracteur == false && Etat_Chaudiere == CHAUDIERE_OFF_NORMAL && (millis() - Temps_Init) > 2*1000 ) { // 2 sec avant extraction
        digitalWrite(EXTRACTEUR,       LOW); // Ouverture Extracteur
        Etat_Extracteur = true;
        delay(1000);
      }
      // ALLUMAGE
      if( Etat_Chaudiere == CHAUDIERE_OFF_NORMAL && Etat_Extracteur == true && (millis() - Temps_Init) > 10*1000 ) { // 10 sec d'attente
        AfficherEtat("Allumage");
        Etat_Chaudiere = CHAUDIERE_ALLUMAGE;
        Temps_Allumage = millis();
        // Relais
        delay(500);
        //digitalWrite(ALLUMEUR,       LOW); // Fin lancement relais ALLUMEUR (mais dure 10 sec)
        //delay(1000);
        digitalWrite(ELECTRO_VANNE,       LOW); // Ouverture Electrovanne GAZ
        delay(300);
        digitalWrite(ELECTRO_MODULATEUR,  HIGH); // Lancement Relais Electromodulateur GAZ
        delay(500); // Attente avant re armement relais temporise
        digitalWrite(ELECTRO_MODULATEUR,  LOW); // FIN Lancement Relais Electromodulateur GAZ
        
        delay(200); // Attente entree gaz dans chaudiere

        digitalWrite(ALLUMEUR,       LOW); // Lancement relais ALLUMEUR
        delay(300); // Etincelles
        digitalWrite(ALLUMEUR,       HIGH); // STOP relais ALLUMEUR
        delay(400); // Repos allumeur
        
        digitalWrite(ALLUMEUR,       LOW); // Lancement relais ALLUMEUR
        delay(300); // Etincelles
        digitalWrite(ALLUMEUR,       HIGH); // STOP relais ALLUMEUR
        delay(400); // Repos allumeur
        
        /*digitalWrite(ALLUMEUR,       LOW); // Lancement relais ALLUMEUR
        delay(300); // Etincelles
        digitalWrite(ALLUMEUR,       HIGH); // STOP relais ALLUMEUR
        delay(400); // Repos allumeur
                
        digitalWrite(ALLUMEUR,       LOW); // Lancement relais ALLUMEUR
        delay(300); // Etincelles
        digitalWrite(ALLUMEUR,       HIGH); // STOP relais ALLUMEUR
        delay(400); // Repos allumeur
        */              
        delay(500); // Attente avant test flamme
      }
    
      // TEST ALLUMAGE
      if( Etat_Chaudiere == CHAUDIERE_ALLUMAGE && (millis() - Temps_Allumage) > 2*1000 ) { // 2 sec pour que les flammes apparaissent
        AfficherEtat("Test Allumage");
        for(int i=0; i<5; i++) {
           Val_Capt_Flamme = analogRead(A0);
           Val_Capt_Flamme_Gauche   = analogRead(A2);
           Serial.print("flammes = "); Serial.print(Val_Capt_Flamme_Gauche);Serial.print("-"); Serial.print(Val_Capt_Flamme); Serial.print(", ");
           delay(500);
        }
      
        // Test de l'allumage
        if(Val_Capt_Flamme < 750 && Val_Capt_Flamme_Gauche < 750 ) { // || Val_Capt_Flamme < 10
          nb_essais_allumage = 0;
          AfficherEtat("Flammes OK");
          Etat_Chaudiere = CHAUDIERE_ON_CHAUFFE;
          Temps_Chauffe = millis();
          delay(1000);
          Temperature_Init = Val_Capt_Temperature_Eau;

          // FIN ALLUMAGE
          digitalWrite(ALLUMEUR,       HIGH); // Logiquement c'est deja arrete
          Relance_Relais_Electromodulateur = 0;
        } else { // ALLUMAGE IMPOSSIBLE
          nb_essais_allumage++; // On compte le nb d'essais d'allumage (3 max)
          ArreterActionneurs(); // on coupe tout
          AfficherEtat("!!!!! PAS de flammes (ou capteurs incoherents) !!!!!");
          // Tentative de relance de l'allumeur
          
          /*for(int rel=0; rel < 20; rel++) { 
            digitalWrite(ALLUMEUR,       LOW); // Lancement relais ALLUMEUR
            delay(200); // Etincelles
            digitalWrite(ALLUMEUR,       HIGH); // STOP relais ALLUMEUR
            delay(200); // Repos allumeur
          }*/
          
          delay(3000);
          if(nb_essais_allumage >= 3) { //
            Etat_Chaudiere = CHAUDIERE_ON_ERREUR;
            nb_essais_allumage = 0;
          } else {
            Temps_Init = millis(); // Tps re initialise
            Etat_Chaudiere = CHAUDIERE_OFF_NORMAL; // pret pour un autre essai
          }
        }
      }
    //} while(Etat_Chaudiere != CHAUDIERE_ON_ERREUR);

    // MAINTIEN REGULIER DE L'ELECTROMODULATEUR (toutes les 10s) avec le RELAIS TEMPORISE
    // Ceci permet de prevenir une panne de l'Arduino et de couper automatiquement
    // l'electromodulateur d'arrivee du GAZ apres 20 secondes (code sur le RELAIS)
    if( Etat_Chaudiere == CHAUDIERE_ON_CHAUFFE) {
      if ( Relance_Relais_Electromodulateur == 0) {
        //Serial.println("Relance Electromodulateur GAZ");
        digitalWrite(ELECTRO_MODULATEUR,  HIGH);   // Relance Relais Electromodulateur GAZ
      } 
      if ( Relance_Relais_Electromodulateur == 8) {
        //Serial.println("FIN Relance Electromodulateur GAZ");
        digitalWrite(ELECTRO_MODULATEUR,  LOW);   // Fin Relance Relais Electromodulateur GAZ        
      }
      Relance_Relais_Electromodulateur = (Relance_Relais_Electromodulateur+1)%10;
    }

    // EXTINCTION
    int temperature_extinction = 40 + 3*(consigne-16);
    //Serial.println(temperature_extinction);
 
    if( Etat_Chaudiere == CHAUDIERE_ON_CHAUFFE &&  (  ((millis() - Temps_Chauffe) > 240000) || Val_Capt_Temperature_Eau >= temperature_extinction || Val_Capt_Temperature_Eau_Bas >= temperature_extinction )  ) { // 4 min (240 sec = 240 000 ms) OU T° >= 60°
      AfficherEtat("Extinction des flammes");
      digitalWrite(ELECTRO_VANNE,       HIGH);  // Fermeture Electrovanne GAZ
      digitalWrite(ELECTRO_MODULATEUR,  LOW);   // Fin Relance Ralais Electromodulateur GAZ
      Etat_Chaudiere = CHAUDIERE_EXTINCTION;
      Temps_Extinction = millis();
      delay(1000);
    }

    // TEST FIN EXTINCTION
    if( Etat_Chaudiere == CHAUDIERE_EXTINCTION && (millis() - Temps_Extinction) > 5*1000 ) { // 5 sec pour que les flammes s'arrêtent
      AfficherEtat("TEST Extinction flammes...");
      for(int i=0; i<5; i++) {
         Val_Capt_Flamme = analogRead(A0);
         Val_Capt_Flamme_Gauche = analogRead(A2);
         Serial.print("flammes = "); Serial.print(Val_Capt_Flamme_Gauche);Serial.print("-"); Serial.print(Val_Capt_Flamme); Serial.print(", ");
         delay(1000);
      }
      Serial.println(".");
      if(Val_Capt_Flamme >= 750 && Val_Capt_Flamme_Gauche >= 750) {
        AfficherEtat("Extinction flammes OK");
        Etat_Chaudiere = CHAUDIERE_REFROIDISSEMENT;
        Temps_Refroidis = millis();
        delay(2000);
        digitalWrite(EXTRACTEUR,       HIGH);  // Arret Extracteur
        Etat_Extracteur = false;
        AfficherEtat("Infos Arret EXTRACTEUR");
      } else {
        AfficherEtat("!!!!! Extinction flammes IMPOSSIBLE (ou capteurs incoherents) => ALARME !!!!!");
        Etat_Chaudiere = CHAUDIERE_ON_ERREUR;
      }
    }
    
    // TEST FIN REFROIDISSEMENT
    int temperature_eau_debut_chauffe = 30 - 3*(25-consigne);  //13 + (3*consigne) - 48 ; // 16 chauffe à 13; 25 => chauffe à 40
    //Serial.print("Rechauffe a:");
    //Serial.println(temperature_eau_debut_chauffe);
    if( Etat_Chaudiere == CHAUDIERE_REFROIDISSEMENT && ((millis() - Temps_Refroidis) > 30000) && 
      ( Val_Capt_Temperature_Eau < temperature_eau_debut_chauffe && Val_Capt_Temperature_Eau_Bas < temperature_eau_debut_chauffe )  ) { // 1 min minimum ET (10 min = 600 sec = 600 000 ms) OU T°<25
      AfficherEtat("FIN de refroidissement");
      Etat_Chaudiere = CHAUDIERE_OFF_NORMAL;
      Temps_Init = millis();
      delay(1000);
    }
    
  //Serial.print(Temperature_Init); Serial.print(F(",")); Serial.println(Val_Capt_Temperature_Eau);
  // DETECTION DES INCOHERENCES
  if(Etat_Chaudiere == CHAUDIERE_ON_CHAUFFE && (Val_Capt_Flamme >= 750 || Val_Capt_Flamme_Gauche >= 750 ) ) { // CHAUFFE mais PLUS DE FLAMME
    AfficherEtat("!!!!! INCOHERENCE : devrait chauffer MAIS pas de flamme (capteurs incoherents ?) !!!!!");
    Etat_Chaudiere = CHAUDIERE_ON_ERREUR;
    AfficherEtat("Fin Incoherence");
  }
  if(Etat_Chaudiere == CHAUDIERE_ON_ERREUR && ( Val_Capt_Flamme < 750 || Val_Capt_Flamme_Gauche < 750 ) ) { // ERREUR mais FLAMME
    AfficherEtat("!!!!! INCOHERENCE : flamme MAIS chaudiere en ERREUR (capteurs flamme incoherents ?) !!!!!");
    Etat_Chaudiere = CHAUDIERE_ON_ERREUR;
  }
  if(Etat_Chaudiere == CHAUDIERE_REFROIDISSEMENT && ( Val_Capt_Flamme < 750 || Val_Capt_Flamme_Gauche < 750 ) ) { // REFROIDISSEMENT mais FLAMME
    AfficherEtat("!!!!! INCOHERENCE : devrait refroidir MAIS flamme (capteurs flamme incoherents ?) !!!!!");
    Etat_Chaudiere = CHAUDIERE_ON_ERREUR;
  }
  if(Etat_Chaudiere == CHAUDIERE_ON_CHAUFFE && abs(Val_Capt_Temperature_Eau - Val_Capt_Temperature_Eau_Bas) > 30 ) { // CAPTEURS DE TEMPERATURE D'EAU TROP DIFFERENTS
    AfficherEtat("!!!!! PB CAPTEURS TEMPERATURE EAU : ECART > 30 degres !!!!!");
    Etat_Chaudiere = CHAUDIERE_ON_ERREUR;
  }
  if( Etat_Chaudiere == CHAUDIERE_ON_CHAUFFE  &&   Val_Capt_Temperature_Eau <=  Temperature_Init + 5 && ((millis() - Temps_Chauffe) > 15000) ) {
    AfficherEtat("!!!!! PB CHAUDIERE EN CHAUFFE DEPUIS 15 sec MAIS DELTA-CHAUFFE < 5° (PB CIRCULATEUR ?) !!!!!");
    Etat_Chaudiere = CHAUDIERE_ON_ERREUR;    
  }

   
  // ************TRAITEMENT URGENCE************
  if( Etat_Chaudiere == CHAUDIERE_ON_ERREUR ) {
    // ARRET D'URGENCE de la chaudiere
    ArreterActionneurs();
    AfficherEtat("!!!!! Mise en ERREUR de la chaudiere !!!!!");
    Etat_Chaudiere = CHAUDIERE_OFF_ERREUR;
  }

  if( Etat_Chaudiere == CHAUDIERE_OFF_ERREUR ) {
      AfficherEtat("!!!!! Infos apres ERREUR : !!!!!");
      Etat_Chaudiere = CHAUDIERE_ARRET_ERREUR;
  }
  //Serial.print("ETAT = ");   Serial.println(Etat_Chaudiere);
  //Serial.print("Flamme = "); Serial.println(Val_Capt_Flamme);
  delay(500);
  
}

