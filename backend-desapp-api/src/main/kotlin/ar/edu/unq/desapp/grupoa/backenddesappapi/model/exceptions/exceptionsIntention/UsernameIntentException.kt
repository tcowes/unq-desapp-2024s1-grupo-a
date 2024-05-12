package ar.edu.unq.desapp.grupoa.backenddesappapi.model.exceptions.exceptionsIntention

class UsernameIntentException(nameUser: String, surnameUser: String) : Exception("Error: $nameUser $surnameUser is not registered to make a BUY/SELL Intent")